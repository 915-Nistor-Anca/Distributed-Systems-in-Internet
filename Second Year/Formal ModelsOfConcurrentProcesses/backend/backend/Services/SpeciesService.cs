using AutoMapper;
using backend.Models;
using backend.Repositories;

namespace backend.Services
{
    public class SpeciesService: ISpeciesService
    {
        private readonly ISpeciesRepository _speciesRepository;
        private readonly IMapper _mapper; 
        public SpeciesService(ISpeciesRepository speciesRepository, IMapper mapper)
        {
            _speciesRepository = speciesRepository;
            _mapper = mapper;
        }
        public async Task<int> AddSpeciesAsync(SpeciesDto species)
        {
            var mappedSpecies = _mapper.Map<Species>(species);
            var speciesId = await _speciesRepository.AddSpeciesAsync(mappedSpecies);
            return speciesId;
        }
        public async Task DeleteSpeciesByIdAsync(int speciesId)
        {
            await _speciesRepository.DeleteSpeciesByIdAsync(speciesId);
        }
        public async Task<PagedResult<SpeciesDto>> GetAllSpeciessAsync(int pageNumber, int pageSize)
        {
            var speciess = await _speciesRepository.GetAllSpeciessAsync(pageNumber, pageSize);
            var mappedSpeciess = _mapper.Map<ICollection<SpeciesDto>>(speciess);
            return new PagedResult<SpeciesDto>
            {
                Items = mappedSpeciess,
                TotalCount = await _speciesRepository.GetTotalNumberOfSpeciessAsync()
            };
        }
        public async Task<SpeciesDto> GetSpeciesByIdAsync(int speciesId)
        {
            var species = await _speciesRepository.GetSpeciesByIdAsync(speciesId);
            var mappedSpecies = _mapper.Map<SpeciesDto>(species);
            return mappedSpecies;
        }
        public async Task UpdateSpeciesAsync(SpeciesDto species)
        {
            var mappedSpecies = _mapper.Map<Species>(species);
            await _speciesRepository.UpdateSpeciesAsync(mappedSpecies);
        }
    }
}
