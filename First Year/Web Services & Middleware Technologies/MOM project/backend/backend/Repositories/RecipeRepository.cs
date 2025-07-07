using backend.Models;
using Microsoft.EntityFrameworkCore;

namespace backend.Repositories
{
    public class RecipeRepository: IRecipeRepository
    {
        private readonly RestaurantDbContext _context;

        public RecipeRepository(RestaurantDbContext context)
        {
            _context = context;
        }

        public async Task<int> AddRecipeAsync(Recipe recipe)
        {
            Validators.RecipeValidator.Validate(recipe);
            await _context.Recipes.AddAsync(recipe);
            await _context.SaveChangesAsync();
            return recipe.Id;
        }

        public async Task DeleteRecipeByIdAsync(int recipeId)
        {
            var recipe = await _context.Recipes.FindAsync(recipeId);
            if (recipe == null)
            {
                throw new Exception($"Recipe with id {recipeId} not found.");
            }
            _context.Recipes.Remove(recipe);
            await _context.SaveChangesAsync();
        }

        public async Task<ICollection<Recipe>> GetAllRecipesAsync(int pageNumber, int pageSize)
        {
            var recipes = await _context.Recipes.AsNoTracking().Skip((pageNumber - 1)*pageSize).Take(pageSize).ToListAsync();
            return recipes;
        }

        public async Task<Recipe> GetRecipeByIdAsync(int recipeId)
        {
            var recipe = await _context.Recipes.AsNoTracking().FirstOrDefaultAsync(x => x.Id == recipeId);
            return recipe;
        }

        public async Task UpdateRecipeAsync(Recipe recipe)
        {
            Validators.RecipeValidator.Validate(recipe);
            var existingRecipe = await _context.Recipes.FindAsync(recipe.Id);
            if (existingRecipe == null)
            {
                throw new Exception($"Recipe with id {recipe.Id} not found.");
            }
            _context.Entry(existingRecipe).State = EntityState.Detached;
            _context.Recipes.Update(recipe);
            await _context.SaveChangesAsync();
        }

        public async Task<int> GetTotalNumberOfRecipesAsync()
        {
            var totalNumberOfRecipes = await _context.Recipes.CountAsync();
            return totalNumberOfRecipes;
        }
    }
}
