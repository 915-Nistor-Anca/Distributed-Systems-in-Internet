using backend.Models;

namespace backend.Validators
{
    public class RecipeValidator
    {
        public static void Validate(Recipe recipe)
        {
            if (string.IsNullOrWhiteSpace(recipe.Name))
                throw new ArgumentException("Name is required.");

            if (string.IsNullOrWhiteSpace(recipe.PreparationSteps))
                throw new ArgumentException("Preparation steps are required.");

            if (string.IsNullOrWhiteSpace(recipe.Ingredients))
                throw new ArgumentException("Ingredients are required.");

            if (recipe.PreparationDate > DateTime.UtcNow)
                throw new ArgumentException("Preparation date cannot be in the future.");
        }
    }
}
