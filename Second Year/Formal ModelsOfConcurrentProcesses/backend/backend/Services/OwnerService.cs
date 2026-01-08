using AutoMapper;
using backend.Models;
using backend.Repositories;

namespace backend.Services
{
    public class OwnerService: IOwnerService
    {
        private readonly IOwnerRepository _ownerRepository;
        private readonly IMapper _mapper; 
        public OwnerService(IOwnerRepository ownerRepository, IMapper mapper)
        {
            _ownerRepository = ownerRepository;
            _mapper = mapper;
        }
        public async Task<int> AddOwnerAsync(OwnerDto owner)
        {
            var mappedOwner = _mapper.Map<Owner>(owner);
            var ownerId = await _ownerRepository.AddOwnerAsync(mappedOwner);
            return ownerId;
        }
        public async Task DeleteOwnerByIdAsync(int ownerId)
        {
            await _ownerRepository.DeleteOwnerByIdAsync(ownerId);
        }
        public async Task<PagedResult<OwnerDto>> GetAllOwnersAsync(int pageNumber, int pageSize)
        {
            var owners = await _ownerRepository.GetAllOwnersAsync(pageNumber, pageSize);
            var mappedOwners = _mapper.Map<ICollection<OwnerDto>>(owners);
            return new PagedResult<OwnerDto>
            {
                Items = mappedOwners,
                TotalCount = await _ownerRepository.GetTotalNumberOfOwnersAsync()
            };
        }
        public async Task<OwnerDto> GetOwnerByIdAsync(int ownerId)
        {
            var owner = await _ownerRepository.GetOwnerByIdAsync(ownerId);
            var mappedOwner = _mapper.Map<OwnerDto>(owner);
            return mappedOwner;
        }
        public async Task UpdateOwnerAsync(OwnerDto owner)
        {
            var mappedOwner = _mapper.Map<Owner>(owner);
            await _ownerRepository.UpdateOwnerAsync(mappedOwner);
        }
    }
}
