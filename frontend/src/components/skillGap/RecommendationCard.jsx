export default function RecommendationCard({ recommendation = {} }) {
  return (
    <article>
      <h3>{recommendation.title ?? "Recommendation"}</h3>
      <p>{recommendation.description}</p>
    </article>
  );
}
