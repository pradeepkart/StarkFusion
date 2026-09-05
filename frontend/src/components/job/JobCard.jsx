export default function JobCard({ job = {} }) {
  return (
    <article>
      <h3>{job.title ?? "Job title"}</h3>
      <p>{job.company}</p>
    </article>
  );
}
