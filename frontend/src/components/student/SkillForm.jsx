import Input from "../common/Input";
import Button from "../common/Button";
export default function SkillForm({ onSubmit }) {
  return (
    <form onSubmit={onSubmit}>
      <Input name="skill" placeholder="Add a skill" />
      <Button type="submit">Save skill</Button>
    </form>
  );
}
