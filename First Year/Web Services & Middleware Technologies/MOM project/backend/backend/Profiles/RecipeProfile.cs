using AutoMapper;
using backend.Models;

namespace backend.Profiles
{
    public class RecipeProfile: Profile
    {
        public RecipeProfile()
        {
            CreateMap<Recipe, RecipeDto>().ReverseMap();
        }
    }
}
