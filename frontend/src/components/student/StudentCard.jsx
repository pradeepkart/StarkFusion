export default function StudentCard({ student = {} }) {
  return (
    <article>
      <h3>{student.name ?? "Student"}</h3>
      <p>{student.email}</p>
    </article>
  );
}
