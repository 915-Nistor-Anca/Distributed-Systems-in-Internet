using AutoMapper;
using backend.Models;
using backend.RabbitMq;
using backend.Repositories;

namespace backend.Services
{
    public class RecipeService: IRecipeService
    {
        private readonly IRecipeRepository _recipeRepository;
        private readonly RabbitMqProducer _rabbitMqProducer;
        private readonly IMapper _mapper; 
        public RecipeService(IRecipeRepository recipeRepository, RabbitMqProducer rabbitMqProducer, IMapper mapper)
        {
            _recipeRepository = recipeRepository;
            _rabbitMqProducer = rabbitMqProducer;
            _mapper = mapper;
        }
        public async Task<int> AddRecipeAsync(RecipeDto recipe)
        {
            var mappedRecipe = _mapper.Map<Recipe>(recipe);
            var recipeId = await _recipeRepository.AddRecipeAsync(mappedRecipe);
            var message = $"New recipe created with ID: {recipeId}";
            await _rabbitMqProducer.SendMessageAsync(message);
            return recipeId;
        }
        public async Task DeleteRecipeByIdAsync(int recipeId)
        {
            await _recipeRepository.DeleteRecipeByIdAsync(recipeId);
        }
        public async Task<PagedResult<RecipeDto>> GetAllRecipesAsync(int pageNumber, int pageSize)
        {
            var recipes = await _recipeRepository.GetAllRecipesAsync(pageNumber, pageSize);
            var mappedRecipes = _mapper.Map<ICollection<RecipeDto>>(recipes);
            return new PagedResult<RecipeDto>
            {
                Items = mappedRecipes,
                TotalCount = await _recipeRepository.GetTotalNumberOfRecipesAsync()
            };
        }
        public async Task<RecipeDto> GetRecipeByIdAsync(int recipeId)
        {
            var recipe = await _recipeRepository.GetRecipeByIdAsync(recipeId);
            var mappedRecipe = _mapper.Map<RecipeDto>(recipe);
            return mappedRecipe;
        }
        public async Task UpdateRecipeAsync(RecipeDto recipe)
        {
            var mappedRecipe = _mapper.Map<Recipe>(recipe);
            await _recipeRepository.UpdateRecipeAsync(mappedRecipe);
        }
    }
}
