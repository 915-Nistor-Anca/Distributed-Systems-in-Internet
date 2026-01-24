using backend.Models;

namespace backend.Repositories
{
    public interface IOwnerRepository
    {
        Task<Owner> AddOwnerAsync(Owner owner);
        Task UpdateOwnerAsync(Owner owner);
        Task<Owner> GetOwnerByIdAsync(int ownerId);
        Task DeleteOwnerByIdAsync(int ownerId);
        Task<ICollection<Owner>> GetAllOwnersAsync(int pageNumber, int pageSize);
        Task<int> GetTotalNumberOfOwnersAsync();
    }
}
