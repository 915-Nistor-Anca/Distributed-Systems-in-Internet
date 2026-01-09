using AutoMapper;
using backend.Models;
using backend.Repositories;
using Microsoft.EntityFrameworkCore;
using System;
using backend.Validators;
using Microsoft.IdentityModel.Tokens;

namespace backend.Services
{
    public class AnimalService: IAnimalService
    {
        private readonly IAnimalRepository _animalRepository;
        private readonly IMapper _mapper; 
        public AnimalService(IAnimalRepository animalRepository, IMapper mapper)
        {
            _animalRepository = animalRepository;
            _mapper = mapper;
        }
        public async Task<Animal> AddAnimalAsync(AnimalUpdateDto animalDto)
        {
            var animal = new Animal
            {
                Name = animalDto.Name,
                Gender = animalDto.Gender,
                BirthDate = animalDto.BirthDate,
                SpeciesId = animalDto.SpeciesId, 
                OwnerId = animalDto.OwnerId    
            };

            AnimalValidator.Validate(animal);

            var mappedAnimal = _mapper.Map<Animal>(animal);
            var addedAnimal = await _animalRepository.AddAnimalAsync(mappedAnimal);
            return addedAnimal;
        }

        public async Task DeleteAnimalByIdAsync(int animalId)
        {
            await _animalRepository.DeleteAnimalByIdAsync(animalId);
        }
        public async Task<PagedResult<AnimalDto>> GetAllAnimalsAsync(int pageNumber, int pageSize, int? speciesId, string? sortBy)
        {
            var animals = await _animalRepository.GetAllAnimalsAsync(pageNumber, pageSize);
            
            if (speciesId.HasValue)
            {
                animals = animals.Where(a => a.Species.Id == speciesId.Value).ToList();
            }

            if (sortBy.IsNullOrEmpty() == false)
            {
                animals = sortBy.ToLower() switch
                {
                    "name" => animals.OrderBy(a => a.Name).ToList(),
                    "birthdate" => animals.OrderBy(a => a.BirthDate).ToList(),
                    _ => animals
                };
            }

            var totalCount = await _animalRepository.GetTotalNumberOfAnimalsAsync();
            if (speciesId.HasValue || !sortBy.IsNullOrEmpty())
            {
                totalCount = animals.Count();
            }
            var mappedAnimals = _mapper.Map<ICollection<AnimalDto>>(animals);
            return new PagedResult<AnimalDto>
            {
                Items = mappedAnimals,
                TotalCount = totalCount
            };
        }
        public async Task<AnimalDto> GetAnimalByIdAsync(int animalId)
        {
            var animal = await _animalRepository.GetAnimalByIdAsync(animalId);
            var mappedAnimal = _mapper.Map<AnimalDto>(animal);
            return mappedAnimal;
        }
        public async Task<Animal> UpdateAnimalAsync(AnimalUpdateDto animal)
        {
            var mappedAnimal = _mapper.Map<Animal>(animal);
            var updated = await _animalRepository.UpdateAnimalAsync(mappedAnimal);
            return updated;
        }

        public async Task<PagedResult<Animal>> SearchByNameAsync(string name)
        {
            var number = await _animalRepository.GetTotalNumberOfAnimalsAsync();

            if (string.IsNullOrWhiteSpace(name))
            {
                return new PagedResult<Animal> { Items = await _animalRepository.GetAllAnimalsAsync(1, 10) , TotalCount = number };
            }

            name = name.Trim().ToLower();
            var allAnimals = await _animalRepository.GetAllAnimalsAsync(1, number);

            var result = allAnimals.Where(a => a.Name != null && a.Name.ToLower().Contains(name)).ToList();

            return new PagedResult<Animal> { Items = result, TotalCount = result.Count};
        }
    }
}
