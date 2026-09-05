import { useEffect, useState } from "react";
import useAuth from "./hooks/useAuth";
import * as auth from "./services/authService";
import * as jobsApi from "./services/jobService";
import * as skillsApi from "./services/skillService";
import * as studentsApi from "./services/studentService";
import * as applicationsApi from "./services/applicationService";
import { getDashboardStats } from "./services/adminService";
import { getSkillGaps } from "./services/skillGapService";
import { getRecommendations } from "./services/recommendationService";
import { APPLICATION_STATUSES, ROLES } from "./utils/constants";
import logo from "./assets/icons/starkfusion-mark.svg";
import "./portal.css";

const levels = ["Beginner", "Basic", "Intermediate", "Advanced", "Expert"];
const values = (form) => Object.fromEntries(new FormData(form));
const errorText = (error) => [error.message || "Something went wrong.", ...Object.values(error.errors || {})].join(" ");

export default function App() {
  const { user } = useAuth();
  return user ? <Workspace key={user.email} user={user} /> : <Login />;
}

function Brand() {
  return <div className="sf-brand"><img src={logo} alt="" /><div>Stark<span>Fusion</span><small>CAREER INTELLIGENCE</small></div></div>;
}

function Login() {
  const { signIn } = useAuth();
  const [mode, setMode] = useState("login");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  async function submit(event) {
    event.preventDefault();
    const account = values(event.currentTarget);
    setBusy(true); setError("");
    try {
      if (mode === "signup") await auth.register(account);
      signIn(await auth.login({ email: account.email, password: account.password }));
    } catch (failure) { setError(errorText(failure)); }
    finally { setBusy(false); }
  }
  return <main className="sf-login"><section className="sf-login-card">
    <Brand /><p className="sf-eyebrow">Your next step starts here</p>
    <h1>{mode === "login" ? "Welcome back." : "Create your profile."}</h1>
    <p className="sf-muted">Connect your skills with real opportunities.</p>
    <div className="sf-tabs">{["login", "signup"].map((item) => <button key={item} disabled={busy} className={mode === item ? "active" : ""} onClick={() => { setMode(item); setError(""); }}>{item === "login" ? "Login" : "Sign up"}</button>)}</div>
    {error && <p className="sf-error" role="alert">{error}</p>}
    <form onSubmit={submit}><fieldset disabled={busy}>
      {mode === "signup" && <Input label="Full name" name="name" maxLength={100} autoComplete="name" />}
      <Input label="Email" name="email" type="email" maxLength={100} autoComplete="username" />
      <Input label="Password" name="password" type="password" minLength={mode === "signup" ? 6 : 1} maxLength={72} autoComplete={mode === "signup" ? "new-password" : "current-password"} />
      <button className="sf-primary sf-wide" type="submit">{busy ? "Please wait…" : mode === "login" ? "Sign in" : "Create account"}</button>
    </fieldset></form>
    <p className="sf-muted sf-footnote">{mode === "signup" ? "Sign up creates a student account." : "Your account determines your admin or student workspace."}</p>
  </section></main>;
}

async function loadWorkspace(admin) {
  const [jobs, catalog, applications, account] = await Promise.all([
    jobsApi.getJobs(), skillsApi.getSkills(),
    admin ? applicationsApi.getApplications() : applicationsApi.getStudentApplications(),
    admin ? getDashboardStats() : studentsApi.getStudentProfile(),
  ]);
  const detailedJobs = await Promise.all(jobs.data.map(async (job) => {
    if (admin) return { ...job, requiredSkills: (await jobsApi.getJobSkills(job.jobId)).data };
    const [analysis, recommendations] = await Promise.all([getSkillGaps(job.jobId), getRecommendations(job.jobId)]);
    return { ...job, analysis: analysis.data, recommendations: recommendations.data };
  }));
  const people = admin ? (await studentsApi.getStudents()).data : (await skillsApi.getStudentSkills()).data;
  return { jobs: detailedJobs, catalog: catalog.data, applications: applications.data, account: account.data, people };
}

function Workspace({ user }) {
  const { signOut } = useAuth();
  const admin = user.role === ROLES.ADMIN;
  const [view, setView] = useState("dashboard");
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [busy, setBusy] = useState(false);
  useEffect(() => {
    let active = true;
    loadWorkspace(admin).then((result) => { if (active) setData(result); }).catch((failure) => { if (active) setError(errorText(failure)); });
    return () => { active = false; };
  }, [admin]);
  async function run(action, message = "Changes saved.") {
    if (busy) return false;
    setBusy(true); setError(""); setNotice("");
    try { await action(); setData(await loadWorkspace(admin)); setNotice(message); return true; }
    catch (failure) { setError(errorText(failure)); return false; }
    finally { setBusy(false); }
  }
  const nav = admin ? ["dashboard", "jobs", "skills", "students", "applications"] : ["dashboard", "jobs", "skills", "gap", "applications", "profile"];
  return <div className="sf-shell"><aside className="sf-sidebar"><Brand />
    <p className="sf-eyebrow">{admin ? "Admin workspace" : "Student workspace"}</p>
    <nav aria-label="Main navigation">{nav.map((item) => <button className={view === item ? "active" : ""} key={item} onClick={() => { setView(item); setError(""); setNotice(""); }}>{item === "gap" ? "Skill gap" : item}</button>)}</nav>
    <div className="sf-account"><strong>{user.name}</strong><small>{user.email}</small><button onClick={signOut}>Sign out</button></div>
  </aside><main className="sf-main">
    <header className="sf-header"><span className="sf-eyebrow">Build your next chapter</span><button className="sf-secondary" disabled={busy} onClick={() => run(async () => {}, "Data refreshed.")}>{busy ? "Saving…" : "Refresh"}</button></header>
    {error && <div className="sf-error" role="alert">{error}</div>}
    {notice && <div className="sf-notice" role="status">{notice}</div>}
    {!data ? <p role="status">{error ? "Use Refresh to try again." : "Loading your workspace…"}</p> : admin ? <AdminViews data={data} view={view} run={run} busy={busy} /> : <StudentViews data={data} view={view} run={run} busy={busy} />}
  </main></div>;
}

function StudentViews({ data, view, run, busy }) {
  const ranked = [...data.jobs].sort((a, b) => b.analysis.overallMatchPercent - a.analysis.overallMatchPercent);
  if (view === "profile") return <><Heading title="My profile" subtitle="Your registered student account." /><section className="sf-card"><h2>{data.account.name}</h2><p>{data.account.email}</p><p className="sf-muted">Student ID: {data.account.studentId}</p></section></>;
  if (view === "skills") return <><Heading title="My skills" subtitle="Choose a catalog skill and set your current proficiency." />
    <form className="sf-card" onSubmit={async (event) => {
      event.preventDefault(); const form = event.currentTarget; const draft = values(form);
      if (await run(() => skillsApi.addStudentSkill({ skillId: Number(draft.skillId), proficiency: Number(draft.proficiency) }))) form.reset();
    }}><fieldset disabled={busy || !data.catalog.length} className="sf-form-grid">
      <SkillSelect catalog={data.catalog} /><LevelSelect name="proficiency" label="Proficiency" />
      <button className="sf-primary">Save skill</button>
    </fieldset>{!data.catalog.length && <p className="sf-muted">No skills are available yet. An administrator needs to add skills to the catalog.</p>}</form>
    <div className="sf-grid">{data.people.map((skill) => <article className="sf-card" key={skill.skillId}><h2>{skill.skillName}</h2><p>{skill.category}</p><p className="sf-muted">Level {skill.proficiency} · {levels[skill.proficiency - 1]}</p><button className="sf-danger" disabled={busy} onClick={() => run(() => skillsApi.removeStudentSkill(skill.skillId), "Skill removed.")}>Remove {skill.skillName}</button></article>)}</div>
    {!data.people.length && <Empty>No skills added yet.</Empty>}
  </>;
  if (view === "applications") return <><Heading title="My applications" subtitle="Application scores are saved at submission." /><Applications items={data.applications} /></>;
  if (view === "gap") return <><Heading title="Skill gap analysis" subtitle="Compare proficiency levels and follow prioritized recommendations." />
    {!ranked.length && <Empty>No jobs have been published yet.</Empty>}
    {ranked.map((job) => <section className="sf-card" key={job.jobId}><h2>{job.title}</h2><p className="sf-muted">{job.company} · {job.analysis.overallMatchPercent}% match</p>
      {!job.analysis.evaluable ? <p>No skill requirements have been configured for this job.</p> : <div className="sf-table-wrap"><table><thead><tr><th>Skill</th><th>Current</th><th>Required</th><th>Gap</th><th>Requirement</th></tr></thead><tbody>{job.analysis.skills.map((skill) => <tr key={skill.skillId}><td>{skill.skillName}</td><td>{skill.currentLevel}</td><td>{skill.requiredLevel}</td><td>{skill.gap}</td><td>{skill.mandatory ? "Mandatory" : "Optional"}</td></tr>)}</tbody></table></div>}
      <h3>Recommendations</h3>{job.recommendations.length ? <ul className="sf-advice">{job.recommendations.map((item) => <li key={item.skillId}><strong>Priority {item.priority}</strong> {item.reason}</li>)}</ul> : <p className="sf-muted">{job.analysis.evaluable ? "All required proficiency levels are met." : "Recommendations appear when requirements are configured."}</p>}
    </section>)}</>;
  return <>{view === "dashboard" ? <><section className="sf-hero"><p className="sf-eyebrow">Your career snapshot</p><h1>Welcome, {data.account.name}.</h1><p>Track your skills and discover your next opportunity.</p></section><Stats items={[["Skills", data.people.length], ["Applications", data.applications.length], ["Best match", `${ranked[0]?.analysis.overallMatchPercent ?? 0}%`]]} /><Heading title="Recommended roles" /></> : <Heading title="Browse jobs" subtitle="Matches use your current proficiency and each job's requirements." />}
    {!ranked.length && <Empty>No jobs have been published yet.</Empty>}
    <div className="sf-grid">{(view === "dashboard" ? ranked.slice(0, 3) : ranked).map((job) => <article className="sf-card" key={job.jobId}><span className="sf-badge">{job.analysis.overallMatchPercent}% match</span><h2>{job.title}</h2><p className="sf-muted">{job.company} · {job.location}</p><div className="sf-tags">{job.analysis.skills.map((skill) => <span key={skill.skillId}>{skill.skillName} · {skill.requiredLevel}</span>)}</div>{!job.analysis.evaluable && <p className="sf-muted">Requirements are not configured yet.</p>}<button className="sf-primary" disabled={busy || data.applications.some((item) => item.jobId === job.jobId)} onClick={() => run(() => applicationsApi.createApplication({ jobId: job.jobId }), "Application submitted.")}>{data.applications.some((item) => item.jobId === job.jobId) ? "Applied" : `Apply for ${job.title}`}</button></article>)}</div>
  </>;
}

function AdminViews({ data, view, run, busy }) {
  const [selectedJob, setSelectedJob] = useState(null);
  const [selectedStudent, setSelectedStudent] = useState(null);
  const [editSkill, setEditSkill] = useState(null);
  if (view === "dashboard") return <><Heading title="Admin dashboard" subtitle="Current activity across the platform." /><Stats items={[["Students", data.account.totalStudents], ["Jobs", data.account.totalJobs], ["Applications", data.account.totalApplications], ["Average application match", `${data.account.averageSkillMatch}%`]]} /><Heading title="Recent applications" /><Applications items={data.applications.slice(0, 10)} admin busy={busy} run={run} /></>;
  if (view === "applications") return <><Heading title="All applications" subtitle="Review applications and update their status." /><Applications items={data.applications} admin busy={busy} run={run} /></>;
  if (view === "students") return <><Heading title="Students" subtitle="Student accounts are created through registration." />{!data.people.length && <Empty>No students have registered yet.</Empty>}<div className="sf-grid">{data.people.map((student) => <article className="sf-card" key={student.studentId}><h2>{student.name}</h2><p>{student.email}</p><button className="sf-secondary" onClick={() => setSelectedStudent(student.studentId)}>View skills for {student.name}</button></article>)}</div>{selectedStudent && <StudentDetails key={selectedStudent} id={selectedStudent} close={() => setSelectedStudent(null)} />}</>;
  if (view === "skills") return <><Heading title="Skill catalog" subtitle="Define the skills available to students and jobs." />
    <form className="sf-card" key={editSkill?.skillId ?? "new"} onSubmit={async (event) => {
      event.preventDefault(); const form = event.currentTarget; const request = values(form);
      if (await run(() => editSkill ? skillsApi.updateSkill(editSkill.skillId, request) : skillsApi.createSkill(request))) { form.reset(); setEditSkill(null); }
    }}><fieldset disabled={busy} className="sf-form-grid"><Input label="Skill name" name="name" maxLength={120} defaultValue={editSkill?.name ?? ""} /><Input label="Category" name="category" maxLength={120} defaultValue={editSkill?.category ?? ""} /><button className="sf-primary">{editSkill ? "Update skill" : "Create skill"}</button>{editSkill && <button type="button" className="sf-secondary" onClick={() => setEditSkill(null)}>Cancel edit</button>}</fieldset></form>
    <div className="sf-grid">{data.catalog.map((skill) => <article className="sf-card" key={skill.skillId}><h2>{skill.name}</h2><p className="sf-muted">{skill.category}</p><div className="sf-actions"><button className="sf-secondary" onClick={() => setEditSkill(skill)}>Edit {skill.name}</button><button className="sf-danger" disabled={busy} onClick={() => run(() => skillsApi.deleteSkill(skill.skillId), "Skill deleted.")}>Delete {skill.name}</button></div></article>)}</div>{!data.catalog.length && <Empty>Create the first skill above.</Empty>}
  </>;
  const job = data.jobs.find((item) => item.jobId === selectedJob);
  return <><Heading title="Manage jobs" subtitle="Publish jobs and define their required proficiency levels." />
    <JobForm busy={busy} submit={async (request) => run(() => jobsApi.createJob(request), "Job created. Add its skill requirements below.")} />
    {!data.jobs.length && <Empty>No jobs yet. Publish your first job above.</Empty>}
    <div className="sf-grid">{data.jobs.map((item) => <article className="sf-card" key={item.jobId}><h2>{item.title}</h2><p className="sf-muted">{item.company} · {item.location}</p><p>{item.requiredSkills.length} skill requirements</p><div className="sf-actions"><button className="sf-secondary" onClick={() => setSelectedJob(item.jobId)}>Manage {item.title}</button><button className="sf-danger" disabled={busy} onClick={async () => { if (await run(() => jobsApi.deleteJob(item.jobId), "Job deleted.")) setSelectedJob(null); }}>Delete {item.title}</button></div></article>)}</div>
    {job && <section className="sf-card"><div className="sf-header"><h2>Manage {job.title}</h2><button className="sf-secondary" onClick={() => setSelectedJob(null)}>Close job editor</button></div>
      <JobForm key={`${job.jobId}-${job.title}-${job.company}-${job.location}`} job={job} busy={busy} submit={(request) => run(() => jobsApi.updateJob(job.jobId, request))} />
      <h3>Required skills</h3><form onSubmit={async (event) => { event.preventDefault(); const draft = values(event.currentTarget); await run(() => jobsApi.addJobSkill(job.jobId, { skillId: Number(draft.skillId), requiredLevel: Number(draft.requiredLevel), mandatory: draft.mandatory === "on" })); }}><fieldset disabled={busy || !data.catalog.length} className="sf-form-grid"><SkillSelect catalog={data.catalog} /><LevelSelect name="requiredLevel" label="Required level" /><label className="sf-checkbox"><input type="checkbox" name="mandatory" defaultChecked /> Mandatory</label><button className="sf-primary">Save requirement</button></fieldset></form>
      {!data.catalog.length && <p className="sf-muted">Create catalog skills before defining requirements.</p>}
      {job.requiredSkills.map((skill) => <div className="sf-requirement" key={skill.skillId}><span>{skill.skillName} · level {skill.requiredLevel} · {skill.mandatory ? "Mandatory" : "Optional"}</span><button className="sf-danger" disabled={busy} onClick={() => run(() => jobsApi.removeJobSkill(job.jobId, skill.skillId), "Requirement removed.")}>Remove {skill.skillName}</button></div>)}
    </section>}
  </>;
}

function StudentDetails({ id, close }) {
  const [detail, setDetail] = useState(null);
  const [error, setError] = useState("");
  useEffect(() => {
    let active = true;
    Promise.all([studentsApi.getStudentById(id), studentsApi.getAdminStudentSkills(id)]).then(([student, skills]) => { if (active) setDetail({ student: student.data, skills: skills.data }); }).catch((failure) => { if (active) setError(errorText(failure)); });
    return () => { active = false; };
  }, [id]);
  return <section className="sf-card"><button className="sf-secondary" onClick={close}>Close student details</button>{error && <p role="alert">{error}</p>}{detail ? <><h2>{detail.student.name}</h2><p>{detail.student.email}</p>{detail.skills.length ? <ul>{detail.skills.map((skill) => <li key={skill.skillId}>{skill.skillName}: {skill.proficiency} / 5</li>)}</ul> : <p>No skills added yet.</p>}</> : !error && <p role="status">Loading student…</p>}</section>;
}

function Applications({ items, admin = false, busy = false, run }) {
  if (!items.length) return <Empty>No applications yet.</Empty>;
  return <div className="sf-card sf-table-wrap"><table><thead><tr><th>Role</th><th>Company</th>{admin && <th>Student</th>}<th>Match</th><th>Status</th></tr></thead><tbody>{items.map((item) => <tr key={item.id}><td>{item.jobTitle}</td><td>{item.company}</td>{admin && <td>{item.studentName}</td>}<td>{item.matchPercent}%</td><td>{admin ? <select aria-label={`Status for ${item.studentName}, ${item.jobTitle}`} disabled={busy} value={item.status} onChange={(event) => run(() => applicationsApi.updateApplicationStatus(item.id, event.target.value), "Application status updated.")}>{APPLICATION_STATUSES.map((status) => <option key={status}>{status}</option>)}</select> : <span className="sf-badge">{item.status}</span>}</td></tr>)}</tbody></table></div>;
}

function JobForm({ job, busy, submit }) {
  return <form className="sf-card" onSubmit={async (event) => { event.preventDefault(); const form = event.currentTarget; if (await submit(values(form)) && !job) form.reset(); }}><fieldset disabled={busy} className="sf-form-grid"><Input label="Job title" name="title" defaultValue={job?.title ?? ""} maxLength={160} /><Input label="Company" name="company" defaultValue={job?.company ?? ""} maxLength={160} /><Input label="Location" name="location" defaultValue={job?.location ?? ""} maxLength={160} /><button className="sf-primary">{job ? "Update job" : "Publish job"}</button></fieldset></form>;
}
function Input({ label, ...props }) { return <label className="sf-field">{label}<input required {...props} /></label>; }
function SkillSelect({ catalog }) { return <label className="sf-field">Skill<select name="skillId" required defaultValue=""><option value="" disabled>Select a skill</option>{catalog.map((skill) => <option key={skill.skillId} value={skill.skillId}>{skill.name}</option>)}</select></label>; }
function LevelSelect({ label, name }) { return <label className="sf-field">{label}<select name={name} defaultValue="3">{levels.map((level, index) => <option key={level} value={index + 1}>{index + 1} · {level}</option>)}</select></label>; }
function Heading({ title, subtitle }) { return <div className="sf-heading"><h1>{title}</h1>{subtitle && <p className="sf-muted">{subtitle}</p>}</div>; }
function Stats({ items }) { return <div className="sf-stats">{items.map(([label, value]) => <div className="sf-card" key={label}><strong>{value}</strong><span>{label}</span></div>)}</div>; }
function Empty({ children }) { return <div className="sf-empty">{children}</div>; }
