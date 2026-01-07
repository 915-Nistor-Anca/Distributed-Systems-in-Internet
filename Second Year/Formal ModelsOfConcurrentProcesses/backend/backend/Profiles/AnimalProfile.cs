using AutoMapper;
using backend.Models;

namespace backend.Profiles
{
    public class AnimalProfile: Profile
    {
        public AnimalProfile()
        {
            CreateMap<Animal, AnimalDto>().ReverseMap();
        }
    }
}
