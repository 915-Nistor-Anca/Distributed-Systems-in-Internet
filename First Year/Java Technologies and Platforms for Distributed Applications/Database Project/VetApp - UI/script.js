const baseUrl = 'http://localhost:8080/VetApp_war_exploded/animals';

document.getElementById('fetchAnimals').addEventListener('click', async () => {
  try {
    const response = await fetch(baseUrl);
    if (!response.ok) throw new Error(await response.text());
    const animals = await response.json();
    const animalItems = document.getElementById('animalItems');
    animalItems.innerHTML = '';
    animals.forEach(animal => {
      const li = document.createElement('li');
      li.textContent = `ID: ${animal.id}, Name: ${animal.name}, Gender: ${animal.gender}, Birth Year: ${animal.birthyear}, Owner: ${animal.owner}`;
      animalItems.appendChild(li);
    });
  } catch (error) {
  }
});

document.getElementById('animalForm').addEventListener('submit', async (event) => {
  event.preventDefault();

  const id = document.getElementById('id').value;
  const name = document.getElementById('name').value;
  const gender = document.getElementById('gender').value;
  const birthyear = document.getElementById('birthyear').value;
  const ownerId = document.getElementById('owner_id').value;
  const specieId = document.getElementById('specie_id').value;
  const vetId = document.getElementById('vet_id').value;

  const method = id ? 'PUT' : 'POST';
  const url = id ? `${baseUrl}?id=${id}` : baseUrl;

  const body = JSON.stringify({
    name, gender, birthyear, owner_id: ownerId, specie_id: specieId, vet_id: vetId,
  });

  try {
    const response = await fetch(url, { method, headers: { 'Content-Type': 'application/json' }, body });
    if (!response.ok) throw new Error(await response.text());
    alert(id ? 'Animal updated successfully!' : 'Animal added successfully!');
    document.getElementById('animalForm').reset();
  } catch (error) {
  }
});

document.getElementById('deleteForm').addEventListener('submit', async (event) => {
  event.preventDefault();

  const id = document.getElementById('deleteId').value;
  try {
    const response = await fetch(`${baseUrl}?id=${id}`, { method: 'DELETE' });
    if (!response.ok) throw new Error(await response.text());
    alert('Animal deleted successfully!');
    document.getElementById('deleteForm').reset();
  } catch (error) {
  }
});

document.addEventListener("DOMContentLoaded", async function () {
    const baseUrl = 'http://localhost:8080/VetApp_war_exploded/animals';

    document.getElementById('fetchAnimals').addEventListener('click', async () => {
      try {
        const response = await fetch(baseUrl);
        if (!response.ok) throw new Error('Error fetching animals');
        const animals = await response.json();
        const animalList = document.getElementById('animalList');
        if (animalList) {
          animalList.innerHTML = '';
          animals.forEach(animal => {
            const li = document.createElement('li');
            
            const owner = parseOwner(animal.owner);

            li.textContent = `ID: ${animal.id}, Name: ${animal.name}, Gender: ${animal.gender}, Birth Year: ${animal.birthyear}, Owner: ${owner.name}, Phone: ${owner.phonenumber}`;
            animalList.appendChild(li);
          });
        }
      } catch (error) {
      }
    });

    document.getElementById('animalForm').addEventListener('submit', async (event) => {
      event.preventDefault();

      const id = document.getElementById('id').value;
      const name = document.getElementById('name').value;
      const gender = document.getElementById('gender').value;
      const birthyear = document.getElementById('birthyear').value;
      const ownerId = document.getElementById('owner_id').value;
      const specieId = document.getElementById('specie_id').value;
      const vetId = document.getElementById('vet_id').value;

      const method = id ? 'PUT' : 'POST';
      const url = id ? `${baseUrl}?id=${id}` : baseUrl;

      const body = new URLSearchParams({
        name, gender, birthyear, owner_id: ownerId, specie_id: specieId, vet_id: vetId,
      });

      try {
        const response = await fetch(url, { method, body });
        if (!response.ok) throw new Error(await response.text());
        alert(id ? 'Animal updated successfully!' : 'Animal added successfully!');
        document.getElementById('animalForm').reset();
      } catch (error) {
      }
    });
  });

  function parseOwner(ownerStr) {
    const regex = /Owner\{id=(\d+), name='([^']+)', phonenumber='([^']+)', email='([^']+)'}/;
    const match = ownerStr.match(regex);
    if (match) {
      return {
        id: match[1],
        name: match[2],
        phonenumber: match[3],
        email: match[4]
      };
    }
    return {};
  }