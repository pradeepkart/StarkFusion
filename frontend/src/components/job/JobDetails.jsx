export default function JobDetails({ job = {} }) {
  return (
    <section>
      <h2>{job.title ?? "Job details"}</h2>
      <p>{job.description}</p>
    </section>
  );
}
