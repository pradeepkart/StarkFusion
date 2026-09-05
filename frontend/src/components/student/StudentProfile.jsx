export default function StudentProfile({ student = {} }) {
  return (
    <section>
      <h2>{student.name ?? "Student profile"}</h2>
    </section>
  );
}
