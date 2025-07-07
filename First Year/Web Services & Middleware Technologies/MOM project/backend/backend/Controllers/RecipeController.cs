using backend.Models;
using backend.Services;
using Microsoft.AspNetCore.Mvc;

namespace backend.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class RecipeController: ControllerBase
    {
        private readonly IRecipeService _recipeService;

        public RecipeController(IRecipeService recipeService)
        {
            _recipeService = recipeService;
        }

        [HttpGet("{id}")]
        public async Task<ActionResult> GetRecipeById(int id)
        {
            var recipe = await _recipeService.GetRecipeByIdAsync(id);
            if (recipe == null)
                return NotFound();

            return Ok(recipe);
        }


        [HttpPost]
        public async Task<ActionResult> AddRecipe(RecipeDto recipe)
        {
            var recipeId = await _recipeService.AddRecipeAsync(recipe);
            return Ok(recipeId);
        }

        [HttpPut]
        public async Task<ActionResult> UpdateRecipe(RecipeDto recipe)
        {
            await _recipeService.UpdateRecipeAsync(recipe);
            return NoContent();
        }

        [HttpDelete("{id}")]
        public async Task<ActionResult> DeleteRecipe(int id)
        {
            await _recipeService.DeleteRecipeByIdAsync(id);
            return NoContent();
        }

        [HttpGet]
        public async Task<ActionResult> GetAllRecipes(int pageNumber, int pageSize)
        {
            var recipes = await _recipeService.GetAllRecipesAsync(pageNumber, pageSize);
            return Ok(recipes);
        }
    }
}
