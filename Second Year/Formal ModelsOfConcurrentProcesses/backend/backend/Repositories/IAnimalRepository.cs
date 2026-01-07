using backend.Models;

namespace backend.Repositories
{
    public interface IAnimalRepository
    {
        Task<int> AddAnimalAsync(Animal animal);
        Task UpdateAnimalAsync(Animal animal);
        Task<Animal> GetAnimalByIdAsync(int animalId);
        Task DeleteAnimalByIdAsync(int animalId);
        Task<ICollection<Animal>> GetAllAnimalsAsync(int pageNumber, int pageSize);
        Task<int> GetTotalNumberOfAnimalsAsync();
    }
}
