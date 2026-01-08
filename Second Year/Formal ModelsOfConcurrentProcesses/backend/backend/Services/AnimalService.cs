using AutoMapper;
using backend.Models;
using backend.Repositories;
using Microsoft.EntityFrameworkCore;
using System;
using backend.Validators;

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
        public async Task<PagedResult<AnimalDto>> GetAllAnimalsAsync(int pageNumber, int pageSize)
        {
            var animals = await _animalRepository.GetAllAnimalsAsync(pageNumber, pageSize);
            var mappedAnimals = _mapper.Map<ICollection<AnimalDto>>(animals);
            return new PagedResult<AnimalDto>
            {
                Items = mappedAnimals,
                TotalCount = await _animalRepository.GetTotalNumberOfAnimalsAsync()
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
    }
}
