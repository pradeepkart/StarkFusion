export default function DashboardStats({ stats = {} }) {
  return (
    <section>
      <p>Students: {stats.students ?? 0}</p>
      <p>Jobs: {stats.jobs ?? 0}</p>
    </section>
  );
}
