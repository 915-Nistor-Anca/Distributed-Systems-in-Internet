using backend.Models;

namespace backend.Services
{
    public interface IRecipeService
    {
        Task<int> AddRecipeAsync(RecipeDto recipe);
        Task UpdateRecipeAsync(RecipeDto recipe);
        Task<RecipeDto> GetRecipeByIdAsync(int recipeId);
        Task DeleteRecipeByIdAsync(int recipeId);
        Task<PagedResult<RecipeDto>> GetAllRecipesAsync(int pageNumber, int pageSize);
    }
}
