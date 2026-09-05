export default function JobTable({ jobs = [] }) {
  return (
    <table>
      <tbody>
        {jobs.map((job) => (
          <tr key={job.id}>
            <td>{job.title}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
