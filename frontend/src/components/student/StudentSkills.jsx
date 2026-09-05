export default function StudentSkills({ skills = [] }) {
  return (
    <ul>
      {skills.map((skill) => (
        <li key={skill.id ?? skill}>{skill.name ?? skill}</li>
      ))}
    </ul>
  );
}
