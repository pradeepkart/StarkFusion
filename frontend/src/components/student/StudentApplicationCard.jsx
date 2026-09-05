export default function StudentApplicationCard({ application = {} }) {
  return (
    <article>
      <h3>{application.jobTitle ?? "Application"}</h3>
      <p>{application.status}</p>
    </article>
  );
}
