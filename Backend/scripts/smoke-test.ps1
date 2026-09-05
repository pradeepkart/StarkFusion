param(
    [string]$BaseUrl = 'http://localhost:8080',
    [string]$AdminEmail = 'admin@skillgap.com',
    [string]$AdminPassword = 'admin123'
)
$ErrorActionPreference = 'Stop'

function Send-Api([string]$Method, [string]$Path, [string]$Token, [hashtable]$Body, [int]$Expected = 200) {
    $parameters = @{Method=$Method; Uri="$BaseUrl/api$Path"; UseBasicParsing=$true; ErrorAction='Stop'}
    if ($Token) { $parameters.Headers = @{Authorization="Bearer $Token"} }
    if ($Body) {
        $parameters.ContentType = 'application/json'
        $parameters.Body = $Body | ConvertTo-Json
    }
    try {
        $response = Invoke-WebRequest @parameters
        $statusCode = [int]$response.StatusCode
        $content = $response.Content
    } catch {
        if (-not $_.Exception.Response) { throw }
        $statusCode = [int]$_.Exception.Response.StatusCode
        $content = ''
    }
    if ($statusCode -ne $Expected) { throw "$Method $Path returned $statusCode; expected $Expected" }
    if ($content) { return ($content | ConvertFrom-Json) }
}
function Assert-Equal($Actual, $Expected, [string]$Message) {
    if ($Actual -ne $Expected) { throw "$Message (expected $Expected, received $Actual)" }
}

$suffix = [guid]::NewGuid().ToString('N').Substring(0, 10)
$email = "student-$suffix@example.com"
$otherEmail = "other-$suffix@example.com"
$null = Send-Api 'POST' '/auth/register' '' @{name='Demo Student'; email=$email; password='123456'} 201
$null = Send-Api 'POST' '/auth/register' '' @{name='Other Student'; email=$otherEmail; password='123456'} 201
$user = Send-Api 'POST' '/auth/login' '' @{email=$email; password='123456'}
$other = Send-Api 'POST' '/auth/login' '' @{email=$otherEmail; password='123456'}
$admin = Send-Api 'POST' '/auth/login' '' @{email=$AdminEmail; password=$AdminPassword}
Assert-Equal $user.role 'ROLE_USER' 'User role'
Assert-Equal $admin.role 'ROLE_ADMIN' 'Admin role'
$null = Send-Api 'GET' '/admin/dashboard' '' $null 401
$null = Send-Api 'GET' '/admin/dashboard' $user.token $null 403
$null = Send-Api 'GET' '/user/profile' $admin.token $null 403

$javaSkill = Send-Api 'POST' '/admin/skills' $admin.token @{name="Java-$suffix"; category='Programming'} 201
$sqlSkill = Send-Api 'POST' '/admin/skills' $admin.token @{name="SQL-$suffix"; category='Database'} 201
$job = Send-Api 'POST' '/admin/jobs' $admin.token @{company='Demo Company'; title='Java Developer'; location='Chennai'} 201
$null = Send-Api 'POST' "/admin/jobs/$($job.jobId)/skills" $admin.token @{skillId=$javaSkill.skillId; requiredLevel=4; mandatory=$true}
$null = Send-Api 'POST' "/admin/jobs/$($job.jobId)/skills" $admin.token @{skillId=$sqlSkill.skillId; requiredLevel=3; mandatory=$false}
$null = Send-Api 'POST' '/user/skills' $user.token @{skillId=$javaSkill.skillId; proficiency=2}
$null = Send-Api 'POST' '/user/skills' $user.token @{skillId=$sqlSkill.skillId; proficiency=3}
$result = Send-Api 'GET' "/user/jobs/$($job.jobId)/skill-gap" $user.token
Assert-Equal $result.overallMatchPercent 66.67 'Weighted match'
$advice = @(Send-Api 'GET' "/user/jobs/$($job.jobId)/recommendations" $user.token)
Assert-Equal $advice.Count 1 'Recommendation count'
Assert-Equal $advice[0].priority 1 'Mandatory gap priority'
$application = Send-Api 'POST' '/user/applications' $user.token @{jobId=$job.jobId} 201
Assert-Equal $application.matchPercent 66.67 'Application match'
$null = Send-Api 'POST' '/user/applications' $user.token @{jobId=$job.jobId} 409
$otherApplications = @(Send-Api 'GET' '/user/applications' $other.token)
Assert-Equal $otherApplications.Count 0 'Other user cannot see application'
$null = Send-Api 'POST' '/user/applications' $other.token @{jobId=$job.jobId; studentId=$application.studentId} 400
$null = Send-Api 'PUT' "/admin/applications/$($application.id)/status" $admin.token @{status='SELECTED'}
$null = Send-Api 'POST' '/user/skills' $user.token @{skillId=$javaSkill.skillId; proficiency=5}
$updated = Send-Api 'GET' "/user/jobs/$($job.jobId)/skill-gap" $user.token
Assert-Equal $updated.overallMatchPercent 100 'Updated match'
$myApplications = @(Send-Api 'GET' '/user/applications' $user.token)
Assert-Equal $myApplications[0].matchPercent 66.67 'Submission snapshot preserved'
Assert-Equal $myApplications[0].status 'SELECTED' 'Admin status is visible'
Write-Output 'MySQL HTTP smoke test passed: authentication, authorization, scoring, isolation and applications.'
[pscustomobject]@{StudentId=$application.studentId; JobId=$job.jobId; ApplicationId=$application.id; MatchPercent=$application.matchPercent}

