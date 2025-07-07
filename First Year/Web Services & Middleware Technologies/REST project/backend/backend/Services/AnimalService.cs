using AutoMapper;
using backend.Models;
using backend.Repositories;

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
        public async Task<int> AddAnimalAsync(AnimalDto animal)
        {
            var mappedAnimal = _mapper.Map<Animal>(animal);
            var animalId = await _animalRepository.AddAnimalAsync(mappedAnimal);
            return animalId;
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
        public async Task UpdateAnimalAsync(AnimalDto animal)
        {
            var mappedAnimal = _mapper.Map<Animal>(animal);
            await _animalRepository.UpdateAnimalAsync(mappedAnimal);
        }
    }
}
