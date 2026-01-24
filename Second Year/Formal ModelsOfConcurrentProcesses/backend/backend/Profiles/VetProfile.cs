using AutoMapper;
using backend.Models;

namespace backend.Profiles
{
    public class VetProfile: Profile
    {
        public VetProfile()
        {
            CreateMap<Animal, AnimalDto>().ReverseMap();
            CreateMap<Animal, AnimalUpdateDto>().ReverseMap();
            CreateMap<Owner, OwnerDto>().ReverseMap();
            CreateMap<Species, SpeciesDto>().ReverseMap();
        }
    }
}
