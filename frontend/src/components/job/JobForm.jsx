import Input from "../common/Input";
import Button from "../common/Button";
export default function JobForm({ onSubmit }) {
  return (
    <form onSubmit={onSubmit}>
      <Input name="title" placeholder="Job title" />
      <Button type="submit">Save job</Button>
    </form>
  );
}
