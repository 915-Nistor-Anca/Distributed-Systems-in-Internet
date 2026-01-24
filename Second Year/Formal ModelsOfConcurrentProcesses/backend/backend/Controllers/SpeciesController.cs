using backend.Models;
using backend.Services;
using Microsoft.AspNetCore.Mvc;

namespace backend.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class SpeciesController: ControllerBase
    {
        private readonly ISpeciesService _speciesService;

        public SpeciesController(ISpeciesService speciesService)
        {
            _speciesService = speciesService;
        }

        [HttpGet("{id}")]
        public async Task<ActionResult> GetSpeciesById(int id)
        {
            var species = await _speciesService.GetSpeciesByIdAsync(id);
            if (species == null)
                return NotFound();

            return Ok(species);
        }


        [HttpPost]
        public async Task<ActionResult> AddSpecies(SpeciesDto species)
        {
            var speciesId = await _speciesService.AddSpeciesAsync(species);
            return Ok(speciesId);
        }

        [HttpPut]
        public async Task<ActionResult> UpdateSpecies(SpeciesDto species)
        {
            await _speciesService.UpdateSpeciesAsync(species);
            return NoContent();
        }

        [HttpDelete("{id}")]
        public async Task<ActionResult> DeleteSpecies(int id)
        {
            await _speciesService.DeleteSpeciesByIdAsync(id);
            return NoContent();
        }

        [HttpGet]
        public async Task<ActionResult> GetAllSpeciess(int pageNumber, int pageSize)
        {
            var speciess = await _speciesService.GetAllSpeciessAsync(pageNumber, pageSize);
            return Ok(speciess);
        }
    }
}
