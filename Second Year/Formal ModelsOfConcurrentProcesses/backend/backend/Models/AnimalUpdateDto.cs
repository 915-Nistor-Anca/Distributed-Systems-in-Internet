namespace backend.Models
{
    public class AnimalUpdateDto
    {
        public int Id { get; set; }
        public string Name { get; set; }
        public int SpeciesId { get; set; }
        public int OwnerId { get; set; }
        public string Gender { get; set; }
        public DateTime BirthDate { get; set; }
    }

}
