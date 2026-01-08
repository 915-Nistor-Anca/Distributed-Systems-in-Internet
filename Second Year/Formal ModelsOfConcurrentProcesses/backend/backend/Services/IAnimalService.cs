using backend.Models;

namespace backend.Services
{
    public interface IAnimalService
    {
        Task<Animal> AddAnimalAsync(AnimalUpdateDto animal);
        Task<Animal> UpdateAnimalAsync(AnimalUpdateDto animal);
        Task<AnimalDto> GetAnimalByIdAsync(int animalId);
        Task DeleteAnimalByIdAsync(int animalId);
        Task<PagedResult<AnimalDto>> GetAllAnimalsAsync(int pageNumber, int pageSize, int? specieId);
        Task<PagedResult<Animal>> SearchByNameAsync(string name);
    }
}
