import Navbar from "../components/common/Navbar";
import Sidebar from "../components/common/Sidebar";
export default function AdminLayout({ children }) {
  return (
    <>
      <Navbar>Admin portal</Navbar>
      <Sidebar>Admin menu</Sidebar>
      <main>{children}</main>
    </>
  );
}
