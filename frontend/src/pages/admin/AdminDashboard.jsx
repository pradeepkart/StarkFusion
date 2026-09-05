import AdminLayout from "../../layouts/AdminLayout";
import DashboardStats from "../../components/admin/DashboardStats";
export default function AdminDashboard() {
  return (
    <AdminLayout>
      <h1>Admin dashboard</h1>
      <DashboardStats />
    </AdminLayout>
  );
}
