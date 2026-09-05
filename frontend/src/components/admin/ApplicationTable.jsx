export default function ApplicationTable({ applications = [] }) {
  return (
    <table>
      <tbody>
        {applications.map((item) => (
          <tr key={item.id}>
            <td>{item.status}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
