import SkillGapCard from "./SkillGapCard";
export default function SkillGapList({ gaps = [] }) {
  return (
    <section>
      {gaps.map((gap) => (
        <SkillGapCard key={gap.id ?? gap.skill} gap={gap} />
      ))}
    </section>
  );
}
