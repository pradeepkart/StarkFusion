export default function StudentTable({ students = [] }) {
  return (
    <table>
      <tbody>
        {students.map((student) => (
          <tr key={student.id}>
            <td>{student.name}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
