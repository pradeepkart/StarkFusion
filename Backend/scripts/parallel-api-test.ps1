param(
    [string]$BaseUrl = 'http://localhost:8080',
    [string]$AdminEmail = 'admin@skillgap.com',
    [string]$AdminPassword = 'admin123',
    [ValidateRange(2,16)][int]$Concurrency = 8,
    [string]$ReportPath = (Join-Path $PSScriptRoot '../.build/parallel-api-results.json')
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Net.Http
[System.Net.ServicePointManager]::DefaultConnectionLimit = 32
$handler = [System.Net.Http.HttpClientHandler]::new()
$handler.UseProxy = $false
$handler.UseCookies = $false
$client = [System.Net.Http.HttpClient]::new($handler)
$client.Timeout = [TimeSpan]::FromSeconds(45)
$script:results = [System.Collections.Generic.List[object]]::new()
$script:maxInFlight = 0

function New-Call([string]$Name, [string]$Method, [string]$Path, [string]$Token, $Body, [int[]]$Expected = @(200)) {
    [pscustomobject]@{Name=$Name; Method=$Method; Path=$Path; Token=$Token; Body=$Body; Expected=$Expected}
}
function Invoke-Batch([object[]]$Calls) {
    for ($offset=0; $offset -lt $Calls.Count; $offset += $Concurrency) {
        $pending = @()
        for ($i=$offset; $i -lt [Math]::Min($offset+$Concurrency,$Calls.Count); $i++) {
            $call = $Calls[$i]
            $message = [System.Net.Http.HttpRequestMessage]::new([System.Net.Http.HttpMethod]::new($call.Method), "$BaseUrl/api$($call.Path)")
            if ($call.Token) { $message.Headers.Authorization = [System.Net.Http.Headers.AuthenticationHeaderValue]::new('Bearer',$call.Token) }
            if ($null -ne $call.Body) {
                $message.Content = [System.Net.Http.StringContent]::new(($call.Body | ConvertTo-Json -Compress),[System.Text.Encoding]::UTF8,'application/json')
            }
            $pending += [pscustomobject]@{Call=$call; Message=$message; Task=$client.SendAsync($message)}
        }
        # All requests above are dispatched before the first response is awaited.
        $script:maxInFlight = [Math]::Max($script:maxInFlight,$pending.Count)
        foreach ($entry in $pending) {
            $response = $null
            try {
                $response = $entry.Task.GetAwaiter().GetResult()
                $status = [int]$response.StatusCode
                $raw = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
                $passed = $entry.Call.Expected -contains $status
                $script:results.Add([pscustomobject]@{Name=$entry.Call.Name;Method=$entry.Call.Method;Path=$entry.Call.Path;Status=$status;Expected=$entry.Call.Expected;Passed=$passed})
                if (-not $passed) { throw "$($entry.Call.Name): HTTP $status, expected $($entry.Call.Expected -join ' or ')" }
                $body = if ($raw) { $raw | ConvertFrom-Json } else { $null }
                [pscustomobject]@{Name=$entry.Call.Name;Status=$status;Body=$body}
            } finally {
                if ($response) { $response.Dispose() }
                $entry.Message.Dispose()
            }
        }
    }
}
function Assert-Equal($Actual,$Expected,[string]$Name) {
    if ($Actual -ne $Expected) { throw "$Name expected $Expected, received $Actual" }
}

$started = [DateTime]::UtcNow
$suffix = [guid]::NewGuid().ToString('N').Substring(0,10)
$complete = $false
try {
    $admin = @(Invoke-Batch @(New-Call 'Admin login' 'POST' '/auth/login' '' @{email=$AdminEmail;password=$AdminPassword}))[0].Body
    $students = @(0..3 | ForEach-Object { [pscustomobject]@{Email="parallel-$suffix-$_@example.com";Token='';Id=0} })
    $null = Invoke-Batch @($students | ForEach-Object { New-Call 'Register' 'POST' '/auth/register' '' @{name='Parallel Student';email=$_.Email;password='123456'} @(201) })
    $logins = @(Invoke-Batch @($students | ForEach-Object { New-Call 'Login' 'POST' '/auth/login' '' @{email=$_.Email;password='123456'} }))
    for ($i=0; $i -lt 4; $i++) { $students[$i].Token = $logins[$i].Body.token }
    $catalog = @(Invoke-Batch @(
        (New-Call 'Create Java' 'POST' '/admin/skills' $admin.token @{name="Java-$suffix";category='Programming'} @(201)),
        (New-Call 'Create SQL' 'POST' '/admin/skills' $admin.token @{name="SQL-$suffix";category='Database'} @(201)),
        (New-Call 'Create job' 'POST' '/admin/jobs' $admin.token @{company='Parallel Test';title="Developer-$suffix";location='Remote'} @(201))
    ))
    $javaId = $catalog[0].Body.skillId
    $sqlId = $catalog[1].Body.skillId
    $jobId = $catalog[2].Body.jobId
    $null = Invoke-Batch @(New-Call 'Require Java' 'POST' "/admin/jobs/$jobId/skills" $admin.token @{skillId=$javaId;requiredLevel=4;mandatory=$true})
    $null = Invoke-Batch @(New-Call 'Require SQL' 'POST' "/admin/jobs/$jobId/skills" $admin.token @{skillId=$sqlId;requiredLevel=3;mandatory=$false})
    $null = Invoke-Batch @($students | ForEach-Object { New-Call 'Add Java skill' 'POST' '/user/skills' $_.Token @{skillId=$javaId;proficiency=2} })
    $null = Invoke-Batch @($students | ForEach-Object { New-Call 'Add SQL skill' 'POST' '/user/skills' $_.Token @{skillId=$sqlId;proficiency=3} })
    $profiles = @(Invoke-Batch @($students | ForEach-Object { New-Call 'Own profile' 'GET' '/user/profile' $_.Token }))
    for ($i=0; $i -lt 4; $i++) { $students[$i].Id = $profiles[$i].Body.studentId }
    $reads = @(Invoke-Batch @($students | ForEach-Object {
        New-Call 'Own skills' 'GET' '/user/skills' $_.Token
        New-Call 'Browse jobs' 'GET' '/user/jobs' $_.Token
        New-Call 'Job details' 'GET' "/user/jobs/$jobId" $_.Token
        New-Call 'Skill gap' 'GET' "/user/jobs/$jobId/skill-gap" $_.Token
        New-Call 'Recommendations' 'GET' "/user/jobs/$jobId/recommendations" $_.Token
        New-Call 'Empty applications' 'GET' '/user/applications' $_.Token
    }))
    foreach ($read in $reads) {
        if ($read.Name -eq 'Skill gap') { Assert-Equal $read.Body.overallMatchPercent 66.67 'Weighted match' }
        if ($read.Name -eq 'Recommendations') { Assert-Equal $read.Body[0].priority 1 'Recommendation priority' }
        if ($read.Name -eq 'Empty applications') { Assert-Equal @($read.Body | Where-Object { $null -ne $_ }).Count 0 'Isolation before applying' }
    }
    $race = @(Invoke-Batch @(1..8 | ForEach-Object { New-Call 'Duplicate application race' 'POST' '/user/applications' $students[0].Token @{jobId=$jobId} @(201,409) }))
    Assert-Equal @($race | Where-Object Status -eq 201).Count 1 'Exactly one application created'
    Assert-Equal @($race | Where-Object Status -eq 409).Count 7 'Duplicate applications rejected'
    $other = @(Invoke-Batch @($students[1..3] | ForEach-Object { New-Call 'Other student applies' 'POST' '/user/applications' $_.Token @{jobId=$jobId} @(201) }))
    $apps = @($race | Where-Object Status -eq 201) + $other
    $null = Invoke-Batch @($apps | ForEach-Object { New-Call 'Select applicant' 'PUT' "/admin/applications/$($_.Body.id)/status" $admin.token @{status='SELECTED'} })
    $own = @(Invoke-Batch @($students | ForEach-Object { New-Call 'Own application' 'GET' '/user/applications' $_.Token }))
    for ($i=0; $i -lt 4; $i++) {
        Assert-Equal @($own[$i].Body).Count 1 'One own application'
        Assert-Equal $own[$i].Body[0].studentId $students[$i].Id 'Student owns application'
        Assert-Equal $own[$i].Body[0].matchPercent 66.67 'Application match snapshot'
        Assert-Equal $own[$i].Body[0].status 'SELECTED' 'Admin status update'
    }
    $null = Invoke-Batch @(
        (New-Call 'Missing token' 'GET' '/user/profile' '' $null @(401)),
        (New-Call 'Invalid token' 'GET' '/user/profile' 'invalid-token' $null @(401)),
        (New-Call 'User denied admin' 'GET' '/admin/dashboard' $students[0].Token $null @(403)),
        (New-Call 'Admin denied user' 'GET' '/user/profile' $admin.token $null @(403)),
        (New-Call 'Injected studentId' 'POST' '/user/applications' $students[1].Token @{jobId=$jobId;studentId=$students[0].Id} @(400)),
        (New-Call 'Invalid proficiency' 'POST' '/user/skills' $students[0].Token @{skillId=$javaId;proficiency=0} @(400)),
        (New-Call 'Missing job' 'GET' '/user/jobs/9223372036854775807' $students[0].Token $null @(404)),
        (New-Call 'Admin dashboard' 'GET' '/admin/dashboard' $admin.token)
    )
    $complete = $true
} finally {
    $client.Dispose()
    $handler.Dispose()
    $report = [pscustomobject]@{
        Completed=$complete;StartedUtc=$started.ToString('o');FinishedUtc=[DateTime]::UtcNow.ToString('o')
        BaseUrl=$BaseUrl;MaxConcurrentRequests=$script:maxInFlight;TotalRequests=$script:results.Count
        Passed=@($script:results | Where-Object Passed).Count
        Failed=@($script:results | Where-Object { -not $_.Passed }).Count
        Checks=$script:results.ToArray()
    }
    New-Item -ItemType Directory -Path (Split-Path -Parent ([System.IO.Path]::GetFullPath($ReportPath))) -Force | Out-Null
    $report | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $ReportPath -Encoding UTF8
}
Write-Output "Parallel API test passed: $($report.TotalRequests) requests, up to $($report.MaxConcurrentRequests) requests in flight."
Write-Output "Report: $ReportPath"
