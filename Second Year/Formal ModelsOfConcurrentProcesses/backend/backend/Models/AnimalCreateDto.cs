namespace backend.Models
{
    public class AnimalCreateDto
    {
        public string Name { get; set; }
        public int SpeciesId { get; set; }
        public int OwnerId { get; set; }
        public string Gender { get; set; }
        public DateTime BirthDate { get; set; }
    }

}
