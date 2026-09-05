import { useEffect, useMemo, useState } from "react";

const seedJobs = [
  {
    id: 1,
    title: "Frontend Developer Intern",
    company: "Nova Labs",
    location: "Remote",
    skills: ["React", "JavaScript", "CSS"],
  },
  {
    id: 2,
    title: "Data Analyst",
    company: "Insight Co.",
    location: "Bengaluru",
    skills: ["Python", "SQL", "Excel"],
  },
  {
    id: 3,
    title: "UI Designer",
    company: "Pixel Studio",
    location: "Chennai",
    skills: ["Figma", "CSS", "Communication"],
  },
];
const seedStudent = {
  id: 1,
  name: "Aarav Kumar",
  email: "aarav@example.com",
  skills: ["React", "JavaScript", "CSS"],
};
const load = (key, fallback) => {
  try {
    return JSON.parse(localStorage.getItem(key)) ?? fallback;
  } catch {
    return fallback;
  }
};

export default function App() {
  const [user, setUser] = useState(() => load("sb-user", null));
  const [jobs, setJobs] = useState(() => load("sb-jobs", seedJobs));
  const [student, setStudent] = useState(() => load("sb-student", seedStudent));
  const [applications, setApplications] = useState(() =>
    load("sb-applications", []),
  );
  const [view, setView] = useState("dashboard");

  useEffect(
    () => localStorage.setItem("sb-jobs", JSON.stringify(jobs)),
    [jobs],
  );
  useEffect(
    () => localStorage.setItem("sb-student", JSON.stringify(student)),
    [student],
  );
  useEffect(
    () => localStorage.setItem("sb-applications", JSON.stringify(applications)),
    [applications],
  );
  useEffect(
    () =>
      user
        ? localStorage.setItem("sb-user", JSON.stringify(user))
        : localStorage.removeItem("sb-user"),
    [user],
  );

  if (!user) {
    return (
      <Login
        onLogin={(account) => {
          if (account.name)
            setStudent((currentStudent) => ({
              ...currentStudent,
              name: account.name,
              email: account.email,
            }));
          setUser({ role: account.role });
          setView("dashboard");
        }}
      />
    );
  }
  return user.role === "admin" ? (
    <Admin
      jobs={jobs}
      setJobs={setJobs}
      applications={applications}
      view={view}
      setView={setView}
      logout={() => setUser(null)}
    />
  ) : (
    <Student
      student={student}
      setStudent={setStudent}
      jobs={jobs}
      applications={applications}
      setApplications={setApplications}
      view={view}
      setView={setView}
      logout={() => setUser(null)}
    />
  );
}

function Login({ onLogin }) {
  const [mode, setMode] = useState("login");
  const [role, setRole] = useState("student");
  const [form, setForm] = useState({ name: "", email: "", password: "" });

  function updateForm(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  function submitForm(event) {
    event.preventDefault();
    onLogin({
      role,
      name: mode === "signup" ? form.name : "",
      email: form.email,
    });
  }

  return (
    <main className="grid min-h-screen place-items-center overflow-hidden bg-[#080d1b] p-5 text-slate-100">
      <div className="pointer-events-none absolute h-[540px] w-[540px] rounded-full bg-fuchsia-500/20 blur-[130px]" />
      <section className="relative w-full max-w-md rounded-[2rem] border border-white/10 bg-slate-950/70 p-8 shadow-2xl backdrop-blur-xl">
        <Brand />
        <p className="mt-8 text-xs font-bold uppercase tracking-[.25em] text-cyan-300">
          Career intelligence network
        </p>
        <h1 className="mt-3 text-3xl font-bold tracking-tight text-white">
          {mode === "login" ? "Welcome back." : "Create your fusion profile."}
        </h1>
        <p className="mt-2 text-slate-400">
          {mode === "login"
            ? "Enter StarkFusion’s adaptive career workspace."
            : "Start matching your skills with opportunity."}
        </p>
        <div className="mt-7 grid grid-cols-2 rounded-xl bg-white/5 p-1">
          <button
            className={`rounded-lg py-2 text-sm font-semibold ${mode === "login" ? "bg-white/10 text-white" : "text-slate-500"}`}
            onClick={() => setMode("login")}
          >
            Login
          </button>
          <button
            className={`rounded-lg py-2 text-sm font-semibold ${mode === "signup" ? "bg-white/10 text-white" : "text-slate-500"}`}
            onClick={() => setMode("signup")}
          >
            Sign up
          </button>
        </div>
        <form className="mt-5 space-y-4" onSubmit={submitForm}>
          {mode === "signup" && (
            <Field
              label="Full name"
              name="name"
              value={form.name}
              onChange={updateForm}
            />
          )}
          <Field
            label="Email"
            name="email"
            type="email"
            value={form.email}
            onChange={updateForm}
            placeholder="you@example.com"
          />
          <Field
            label="Password"
            name="password"
            type="password"
            value={form.password}
            onChange={updateForm}
            placeholder="At least 6 characters"
          />
          <label className="block text-sm font-semibold text-slate-300">
            Portal
            <select
              className="mt-1 w-full rounded-xl border border-white/10 bg-slate-900 p-3 text-white"
              value={role}
              onChange={(event) => setRole(event.target.value)}
            >
              <option value="student">Student command center</option>
              <option value="admin">Admin command center</option>
            </select>
          </label>
          <button className="w-full rounded-xl bg-gradient-to-r from-cyan-400 via-indigo-400 to-fuchsia-400 py-3 font-bold text-slate-950 transition hover:scale-[1.01]">
            {mode === "login" ? "Enter portal" : "Create account"}
          </button>
        </form>
        <p className="mt-5 text-center text-xs text-slate-500">
          Demo mode: data is saved in this browser.
        </p>
      </section>
    </main>
  );
}

function Student({
  student,
  setStudent,
  jobs,
  applications,
  setApplications,
  view,
  setView,
  logout,
}) {
  const matches = useMemo(
    () =>
      jobs
        .map((job) => ({
          ...job,
          score: Math.round(
            (job.skills.filter((skill) =>
              student.skills
                .map((value) => value.toLowerCase())
                .includes(skill.toLowerCase()),
            ).length *
              100) /
              job.skills.length,
          ),
        }))
        .sort((a, b) => b.score - a.score),
    [jobs, student.skills],
  );
  const [skill, setSkill] = useState("");
  const apply = (job) => {
    if (!applications.some((item) => item.jobId === job.id))
      setApplications([
        ...applications,
        {
          id: Date.now(),
          jobId: job.id,
          title: job.title,
          company: job.company,
          status: "Submitted",
          date: new Date().toLocaleDateString(),
        },
      ]);
  };
  const content =
    view === "dashboard" ? (
      <>
        <Hero student={student} />
        <Stats
          values={[
            ["Skills", student.skills.length],
            ["Applications", applications.length],
            ["Best match", `${matches[0]?.score ?? 0}%`],
          ]}
        />
        <Heading
          title="Recommended roles"
          subtitle="Ranked by your current skills."
        />
        <div className="grid gap-5 md:grid-cols-3">
          {matches.slice(0, 3).map((job) => (
            <JobCard
              key={job.id}
              job={job}
              applied={applications.some((item) => item.jobId === job.id)}
              apply={apply}
            />
          ))}
        </div>
      </>
    ) : view === "jobs" ? (
      <Page title="Browse jobs" subtitle="Find roles that fit your profile.">
        <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
          {matches.map((job) => (
            <JobCard
              key={job.id}
              job={job}
              applied={applications.some((item) => item.jobId === job.id)}
              apply={apply}
            />
          ))}
        </div>
      </Page>
    ) : view === "skills" ? (
      <Page
        title="My skills"
        subtitle="Keep this list current to improve matching."
      >
        <form
          className="mb-6 flex max-w-lg gap-2"
          onSubmit={(e) => {
            e.preventDefault();
            const value = skill.trim();
            if (value && !student.skills.includes(value))
              setStudent({ ...student, skills: [...student.skills, value] });
            setSkill("");
          }}
        >
          <input
            className="min-w-0 flex-1 rounded-lg border border-slate-300 px-3"
            value={skill}
            onChange={(e) => setSkill(e.target.value)}
            placeholder="Add a skill"
          />
          <button className="rounded-lg bg-teal-600 px-4 py-2 font-semibold text-white">
            Add
          </button>
        </form>
        <div className="flex flex-wrap gap-2">
          {student.skills.map((item) => (
            <button
              className="rounded-full bg-teal-50 px-4 py-2 text-sm font-medium text-teal-800"
              onClick={() =>
                setStudent({
                  ...student,
                  skills: student.skills.filter((value) => value !== item),
                })
              }
              key={item}
            >
              {item} ×
            </button>
          ))}
        </div>
      </Page>
    ) : view === "gap" ? (
      <Page
        title="Skill gap analysis"
        subtitle="Core skills missing from each available role."
      >
        <div className="space-y-3">
          {matches.map((job) => (
            <div
              className="rounded-xl border border-slate-200 bg-white p-5"
              key={job.id}
            >
              <div className="flex flex-wrap justify-between gap-2">
                <div>
                  <h3 className="font-bold">{job.title}</h3>
                  <p className="text-sm text-slate-500">
                    {job.company} · {job.score}% match
                  </p>
                </div>
                <div className="flex flex-wrap gap-2">
                  {job.skills
                    .filter(
                      (x) =>
                        !student.skills
                          .map((s) => s.toLowerCase())
                          .includes(x.toLowerCase()),
                    )
                    .map((x) => (
                      <span
                        className="rounded-full bg-orange-50 px-3 py-1 text-sm text-orange-700"
                        key={x}
                      >
                        {x}
                      </span>
                    )) || (
                    <span className="text-teal-700">
                      All core skills covered
                    </span>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      </Page>
    ) : (
      <Page
        title="My applications"
        subtitle="Every application submitted from the portal."
      >
        <Applications items={applications} />
      </Page>
    );
  return (
    <Shell
      nav={["dashboard", "jobs", "skills", "gap", "applications"]}
      view={view}
      setView={setView}
      logout={logout}
    >
      {content}
    </Shell>
  );
}

function Admin({ jobs, setJobs, applications, view, setView, logout }) {
  const [draft, setDraft] = useState({
    title: "",
    company: "",
    location: "",
    skills: "",
  });
  const content =
    view === "dashboard" ? (
      <>
        <Stats
          values={[
            ["Live jobs", jobs.length],
            ["Applications", applications.length],
            ["Students", 1],
          ]}
        />
        <Heading title="Recent applications" />
        <Applications items={applications} />
      </>
    ) : view === "jobs" ? (
      <Page title="Manage jobs" subtitle="Publish and remove openings.">
        <form
          className="mb-6 grid gap-3 rounded-xl border border-slate-200 bg-white p-5 md:grid-cols-2"
          onSubmit={(e) => {
            e.preventDefault();
            if (!draft.title || !draft.company) return;
            setJobs([
              ...jobs,
              {
                ...draft,
                id: Date.now(),
                skills: draft.skills
                  .split(",")
                  .map((x) => x.trim())
                  .filter(Boolean),
              },
            ]);
            setDraft({ title: "", company: "", location: "", skills: "" });
          }}
        >
          {Object.entries(draft).map(([key, value]) => (
            <input
              className="rounded-lg border border-slate-300 px-3 py-2"
              placeholder={
                key === "skills"
                  ? "Skills (comma separated)"
                  : key[0].toUpperCase() + key.slice(1)
              }
              value={value}
              onChange={(e) => setDraft({ ...draft, [key]: e.target.value })}
              key={key}
            />
          ))}
          <button className="rounded-lg bg-teal-600 px-4 py-2 font-bold text-white">
            Publish job
          </button>
        </form>
        <div className="grid gap-5 md:grid-cols-2">
          {jobs.map((job) => (
            <div
              className="rounded-xl border border-slate-200 bg-white p-5"
              key={job.id}
            >
              <h3 className="font-bold">{job.title}</h3>
              <p className="text-sm text-slate-500">
                {job.company} · {job.location}
              </p>
              <button
                className="mt-4 text-sm font-semibold text-rose-600"
                onClick={() =>
                  setJobs(jobs.filter((item) => item.id !== job.id))
                }
              >
                Remove listing
              </button>
            </div>
          ))}
        </div>
      </Page>
    ) : view === "students" ? (
      <Page title="Students">
        <div className="rounded-xl border border-slate-200 bg-white p-5">
          <b>Aarav Kumar</b>
          <p className="mt-1 text-slate-500">
            aarav@example.com · React, JavaScript, CSS
          </p>
        </div>
      </Page>
    ) : (
      <Page title="All applications">
        <Applications items={applications} />
      </Page>
    );
  return (
    <Shell
      nav={["dashboard", "jobs", "students", "applications"]}
      view={view}
      setView={setView}
      logout={logout}
    >
      {content}
    </Shell>
  );
}

function Shell({ nav, view, setView, logout, children }) {
  return (
    <div className="min-h-screen bg-[#080d1b] text-slate-100 md:flex">
      <aside className="relative flex overflow-hidden border-b border-white/10 bg-[#0d1428] p-5 md:min-h-screen md:w-72 md:flex-col md:border-b-0 md:border-r">
        <div className="absolute -left-16 top-20 h-44 w-44 rounded-full bg-cyan-400/15 blur-3xl" />
        <Brand />
        <p className="relative mt-7 hidden text-[10px] font-bold uppercase tracking-[.22em] text-slate-500 md:block">
          Fusion modules
        </p>
        <nav className="relative ml-6 flex gap-2 overflow-auto md:ml-0 md:mt-3 md:flex-col">
          {nav.map((item, index) => (
            <button
              className={`rounded-xl px-3 py-3 text-left text-sm font-semibold capitalize transition ${view === item ? "bg-gradient-to-r from-cyan-400/15 to-indigo-400/15 text-cyan-200 ring-1 ring-cyan-300/20" : "text-slate-400 hover:bg-white/5 hover:text-white"}`}
              onClick={() => setView(item)}
              key={item}
            >
              <span className="mr-3 text-xs text-cyan-300/70">
                0{index + 1}
              </span>
              {item.replace("gap", "skill gap")}
            </button>
          ))}
        </nav>
        <button
          className="relative ml-auto text-sm text-rose-300 md:ml-0 md:mt-auto"
          onClick={logout}
        >
          ◌ Disconnect
        </button>
      </aside>
      <main className="relative mx-auto w-full max-w-6xl overflow-hidden p-6 md:p-10">
        <div className="pointer-events-none absolute right-0 top-0 h-80 w-80 rounded-full bg-indigo-500/10 blur-[100px]" />
        <div className="relative">{children}</div>
      </main>
    </div>
  );
}
function Brand() {
  return (
    <div className="relative flex items-center gap-3">
      <img
        className="h-10 w-10"
        src="/src/assets/icons/starkfusion-mark.svg"
        alt="StarkFusion logo"
      />
      <div className="text-xl font-bold tracking-tight text-white">
        Stark
        <span className="bg-gradient-to-r from-cyan-300 to-fuchsia-300 bg-clip-text text-transparent">
          Fusion
        </span>
        <p className="mt-0.5 text-[9px] font-bold uppercase tracking-[.25em] text-slate-500">
          intelligence hub
        </p>
      </div>
    </div>
  );
}
function Field({ label, name, type = "text", value, onChange, placeholder }) {
  return (
    <label className="block text-sm font-semibold text-slate-300">
      {label}
      <input
        className="mt-1 w-full rounded-xl border border-white/10 bg-slate-900 p-3 text-white outline-none placeholder:text-slate-600 focus:border-cyan-300/60"
        name={name}
        type={type}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        required
      />
    </label>
  );
}
function Hero({ student }) {
  return (
    <section className="relative overflow-hidden rounded-[1.75rem] border border-white/10 bg-gradient-to-br from-[#151d3a] via-[#101a32] to-[#261339] p-7 text-white">
      <div className="absolute -right-10 -top-10 h-44 w-44 rounded-full border border-cyan-300/20" />
      <div className="absolute right-14 top-8 h-20 w-20 rounded-full border border-fuchsia-300/20" />
      <p className="relative text-xs font-bold uppercase tracking-[.25em] text-cyan-300">
        Fusion signal / career snapshot
      </p>
      <h1 className="relative mt-3 text-3xl font-bold tracking-tight">
        Ready to ignite, {student.name.split(" ")[0]}?
      </h1>
      <p className="relative mt-2 max-w-xl text-slate-300">
        Your skills are mapped against live opportunity signals in real time.
      </p>
    </section>
  );
}
function Stats({ values }) {
  return (
    <section className="my-6 grid gap-4 sm:grid-cols-3">
      {values.map(([label, value]) => (
        <div
          className="rounded-xl border border-slate-200 bg-white p-5"
          key={label}
        >
          <strong className="block text-3xl text-teal-700">{value}</strong>
          <span className="text-sm text-slate-500">{label}</span>
        </div>
      ))}
    </section>
  );
}
function Heading({ title, subtitle }) {
  return (
    <div className="mb-5">
      <h2 className="text-2xl font-bold text-slate-800">{title}</h2>
      {subtitle && <p className="mt-1 text-slate-500">{subtitle}</p>}
    </div>
  );
}
function Page({ title, subtitle, children }) {
  return (
    <>
      <Heading title={title} subtitle={subtitle} />
      {children}
    </>
  );
}
function JobCard({ job, applied, apply }) {
  return (
    <article className="flex flex-col rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
      <span className="self-start rounded-full bg-teal-50 px-3 py-1 text-xs font-bold text-teal-700">
        {job.score}% match
      </span>
      <h3 className="mt-4 font-bold text-slate-900">{job.title}</h3>
      <p className="mt-1 text-sm text-slate-500">
        {job.company} · {job.location}
      </p>
      <div className="mt-4 flex flex-wrap gap-2">
        {job.skills.map((item) => (
          <span
            className="rounded-full bg-slate-100 px-2 py-1 text-xs text-slate-600"
            key={item}
          >
            {item}
          </span>
        ))}
      </div>
      <button
        disabled={applied}
        className="mt-5 rounded-lg bg-teal-600 px-3 py-2 font-semibold text-white disabled:bg-slate-200 disabled:text-slate-500"
        onClick={() => apply(job)}
      >
        {applied ? "Applied" : "Apply now"}
      </button>
    </article>
  );
}
function Applications({ items }) {
  return items.length ? (
    <div className="overflow-auto rounded-xl border border-slate-200 bg-white">
      <table className="w-full min-w-[520px] text-left text-sm">
        <thead className="bg-slate-50 text-slate-500">
          <tr>
            <th className="p-4">Role</th>
            <th>Company</th>
            <th>Status</th>
            <th>Applied</th>
          </tr>
        </thead>
        <tbody>
          {items.map((item) => (
            <tr className="border-t border-slate-100" key={item.id}>
              <td className="p-4 font-semibold">{item.title}</td>
              <td>{item.company}</td>
              <td>
                <span className="rounded-full bg-teal-50 px-2 py-1 text-xs text-teal-700">
                  {item.status}
                </span>
              </td>
              <td>{item.date}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  ) : (
    <div className="rounded-xl border border-dashed border-slate-300 bg-white p-10 text-center text-slate-500">
      No applications yet.
    </div>
  );
}
