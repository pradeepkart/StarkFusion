import Input from "../common/Input";
export default function StudentForm(props) {
  return (
    <form {...props}>
      <Input name="name" placeholder="Student name" />
    </form>
  );
}
