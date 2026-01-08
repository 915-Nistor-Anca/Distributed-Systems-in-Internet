using backend.Models;

namespace backend.Services
{
    public interface IAnimalService
    {
        Task<int> AddAnimalAsync(AnimalUpdateDto animal);
        Task UpdateAnimalAsync(AnimalUpdateDto animal);
        Task<AnimalDto> GetAnimalByIdAsync(int animalId);
        Task DeleteAnimalByIdAsync(int animalId);
        Task<PagedResult<AnimalDto>> GetAllAnimalsAsync(int pageNumber, int pageSize);
    }
}
