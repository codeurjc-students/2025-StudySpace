const validRooms = [];
for (let i = 1; i <= 113; i++) {
  if (i !== 11 && i !== 12) {
    validRooms.push(i);
  }
}
const MAX_CAMPUS_ID = 5; 

const searchKeywords = [
  "Laboratorio", "Aula", "Mac", "Windows", "Linux", 
  "Proyector", "Reuniones", "204", "Informatica", "Eclipse" ,"Magna"
];

module.exports = {
  setupDatesAndRooms: function(context, events, done) {
    
    // Random user (from 1 to 100)
    const userNumber = Math.floor(Math.random() * 100) + 1;
    context.vars.email = `testuser${userNumber}@urjc.es`;
    context.vars.password = "1234aA.."; 

    // Random room (excluding under maintenance 11 y 12 rooms )
    const roomIndex = Math.floor(Math.random() * validRooms.length);
    context.vars.roomId = validRooms[roomIndex].toString();

    // 2000 days to avoid overpassing the 3 hour per day clausure)
    const offsetDays = Math.floor(Math.random() * 2000) + 30; 
    
    let targetDate = new Date();
    targetDate.setDate(targetDate.getDate() + offsetDays);

    if (targetDate.getDay() === 0) targetDate.setDate(targetDate.getDate() + 1);
    if (targetDate.getDay() === 6) targetDate.setDate(targetDate.getDate() + 2);

    const year = targetDate.getFullYear();
    const month = String(targetDate.getMonth() + 1).padStart(2, '0');
    const day = String(targetDate.getDate()).padStart(2, '0');

    //only 1 hour
    const startHour = Math.floor(Math.random() * 8) + 10; // Between 10:00 and 17:00
    const endHour = startHour + 1; 

    const strStartHour = String(startHour).padStart(2, '0');
    const strEndHour = String(endHour).padStart(2, '0');

    // UTC (Z)
    const testStartDate = `${year}-${month}-${day}T${strStartHour}:00:00.000Z`;
    const testEndDate = `${year}-${month}-${day}T${strEndHour}:00:00.000Z`;

    context.vars.dynamicStartDate = testStartDate;
    context.vars.dynamicEndDate = testEndDate;
    context.vars.encodedStartDate = encodeURIComponent(testStartDate);
    context.vars.encodedEndDate = encodeURIComponent(testEndDate);

    // Random search
    const randomKeyword = searchKeywords[Math.floor(Math.random() * searchKeywords.length)];
    const randomCampus = Math.floor(Math.random() * MAX_CAMPUS_ID) + 1;
    const randomCapacity = Math.floor(Math.random() * 30) + 10; 

    context.vars.searchText = encodeURIComponent(randomKeyword);
    context.vars.searchCampusId = randomCampus.toString();
    context.vars.searchCapacity = randomCapacity.toString();
    
    return done();
  }
};