namespace backend.Models
{
    public class RecipeDto
    {
        public int Id { get; set; }
        public string Name { get; set; }
        public string PreparationSteps { get; set; }
        public string Ingredients { get; set; }
        public bool IsVegetarian { get; set; }
        public DateTime PreparationDate { get; set; }
    }
}
