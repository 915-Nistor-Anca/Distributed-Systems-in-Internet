using backend.Models;

namespace backend.Services
{
    public interface IOwnerService
    {
        Task<Owner> AddOwnerAsync(OwnerDto owner);
        Task UpdateOwnerAsync(OwnerDto owner);
        Task<OwnerDto> GetOwnerByIdAsync(int ownerId);
        Task DeleteOwnerByIdAsync(int ownerId);
        Task<PagedResult<OwnerDto>> GetAllOwnersAsync(int pageNumber, int pageSize);
    }
}
