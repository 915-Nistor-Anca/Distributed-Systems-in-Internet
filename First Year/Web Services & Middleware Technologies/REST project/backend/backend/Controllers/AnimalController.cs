using backend.Models;
using backend.Services;
using Microsoft.AspNetCore.Mvc;

namespace backend.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class AnimalController: ControllerBase
    {
        private readonly IAnimalService _animalService;

        public AnimalController(IAnimalService animalService)
        {
            _animalService = animalService;
        }

        [HttpGet("{id}")]
        public async Task<ActionResult> GetAnimalById(int id)
        {
            var animal = await _animalService.GetAnimalByIdAsync(id);
            if (animal == null)
                return NotFound();

            return Ok(animal);
        }


        [HttpPost]
        public async Task<ActionResult> AddAnimal(AnimalDto animal)
        {
            var animalId = await _animalService.AddAnimalAsync(animal);
            return Ok(animalId);
        }

        [HttpPut]
        public async Task<ActionResult> UpdateAnimal(AnimalDto animal)
        {
            await _animalService.UpdateAnimalAsync(animal);
            return NoContent();
        }

        [HttpDelete("{id}")]
        public async Task<ActionResult> DeleteAnimal(int id)
        {
            await _animalService.DeleteAnimalByIdAsync(id);
            return NoContent();
        }

        [HttpGet]
        public async Task<ActionResult> GetAllAnimals(int pageNumber, int pageSize)
        {
            var animals = await _animalService.GetAllAnimalsAsync(pageNumber, pageSize);
            return Ok(animals);
        }
    }
}
