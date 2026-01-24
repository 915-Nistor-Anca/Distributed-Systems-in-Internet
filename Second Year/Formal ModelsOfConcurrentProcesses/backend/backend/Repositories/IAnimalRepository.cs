using backend.Models;

namespace backend.Repositories
{
    public interface IAnimalRepository
    {
        Task<Animal> AddAnimalAsync(Animal animal);
        Task<Animal> UpdateAnimalAsync(Animal animal);
        Task<Animal> GetAnimalByIdAsync(int animalId);
        Task DeleteAnimalByIdAsync(int animalId);
        Task<ICollection<Animal>> GetAllAnimalsAsync(int pageNumber, int pageSize);
        Task<int> GetTotalNumberOfAnimalsAsync();
    }
}
