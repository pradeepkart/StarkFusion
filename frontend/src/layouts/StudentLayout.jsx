import Navbar from "../components/common/Navbar";
import Sidebar from "../components/common/Sidebar";
export default function StudentLayout({ children }) {
  return (
    <>
      <Navbar>Student portal</Navbar>
      <Sidebar>Student menu</Sidebar>
      <main>{children}</main>
    </>
  );
}
