using backend.Models;
using backend.Services;
using Microsoft.AspNetCore.Mvc;

namespace backend.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class OwnerController: ControllerBase
    {
        private readonly IOwnerService _ownerService;

        public OwnerController(IOwnerService ownerService)
        {
            _ownerService = ownerService;
        }

        [HttpGet("{id}")]
        public async Task<ActionResult> GetOwnerById(int id)
        {
            var owner = await _ownerService.GetOwnerByIdAsync(id);
            if (owner == null)
                return NotFound();

            return Ok(owner);
        }


        [HttpPost]
        public async Task<ActionResult> AddOwner(OwnerDto owner)
        {
            var addedOwner = await _ownerService.AddOwnerAsync(owner);
            return Ok(addedOwner);
        }

        [HttpPut]
        public async Task<ActionResult> UpdateOwner(OwnerDto owner)
        {
            await _ownerService.UpdateOwnerAsync(owner);
            return NoContent();
        }

        [HttpDelete("{id}")]
        public async Task<ActionResult> DeleteOwner(int id)
        {
            await _ownerService.DeleteOwnerByIdAsync(id);
            return NoContent();
        }

        [HttpGet]
        public async Task<ActionResult> GetAllOwners(int pageNumber, int pageSize)
        {
            var owners = await _ownerService.GetAllOwnersAsync(pageNumber, pageSize);
            return Ok(owners);
        }
    }
}
