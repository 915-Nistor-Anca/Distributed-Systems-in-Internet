using backend.Models;

namespace backend.Services
{
    public interface IAnimalService
    {
        Task<int> AddAnimalAsync(AnimalDto animal);
        Task UpdateAnimalAsync(AnimalDto animal);
        Task<AnimalDto> GetAnimalByIdAsync(int animalId);
        Task DeleteAnimalByIdAsync(int animalId);
        Task<PagedResult<AnimalDto>> GetAllAnimalsAsync(int pageNumber, int pageSize);
    }
}
