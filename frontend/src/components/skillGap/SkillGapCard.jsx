export default function SkillGapCard({ gap = {} }) {
  return (
    <article>
      <h3>{gap.skill ?? "Skill gap"}</h3>
      <p>{gap.level}</p>
    </article>
  );
}
