using backend.Models;

namespace backend.Repositories
{
    public interface IRecipeRepository
    {
        Task<int> AddRecipeAsync(Recipe recipe);
        Task UpdateRecipeAsync(Recipe recipe);
        Task<Recipe> GetRecipeByIdAsync(int recipeId);
        Task DeleteRecipeByIdAsync(int recipeId);
        Task<ICollection<Recipe>> GetAllRecipesAsync(int pageNumber, int pageSize);
        Task<int> GetTotalNumberOfRecipesAsync();
    }
}
