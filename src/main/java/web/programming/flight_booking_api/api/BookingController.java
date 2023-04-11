package web.programming.flight_booking_api.api;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import web.programming.flight_booking_api.api.dto.BookingCreateDto;
import web.programming.flight_booking_api.api.dto.BookingDto;
import web.programming.flight_booking_api.api.dto.BookingMapper;
import web.programming.flight_booking_api.entidades.Booking;
import web.programming.flight_booking_api.entidades.BookingStatus;
import web.programming.flight_booking_api.exceptions.BookingNotFoundException;
import web.programming.flight_booking_api.services.BookingService;
@RestController
@RequestMapping("/api/model2")
public class BookingController {
    @Autowired
    private final BookingService bookingService;
    @Autowired
    private final BookingMapper bookingMapper;
    public BookingController(BookingService bookingService, BookingMapper bookingMapper) {
        this.bookingService = bookingService;
        this.bookingMapper = bookingMapper;
    }
    @GetMapping("/bookings")
    public ResponseEntity<List<BookingCreateDto>> getAllBookings( @RequestParam(required = false) String firstName,
    @RequestParam(required = false) String status)
    {
        BookingStatus bookingStatus = null;
        if(status != null)
        {
            try
            {
                bookingStatus = BookingStatus.valueOf(status);
            }
            catch(IllegalArgumentException e)
            {
                return ResponseEntity.badRequest().build();
            }
        }
        List<Booking> bookings = bookingService.find1(firstName, bookingStatus);
        List<BookingCreateDto> bookingsDto = bookings.stream().map(bookingMapper::toCreateDto).collect(Collectors.toList());
        if(bookingsDto.isEmpty())
            return ResponseEntity.noContent().build();
        else{
            return ResponseEntity.ok(bookingsDto);
        }
    }
    @GetMapping("/bookings/{id}")
    public ResponseEntity<BookingCreateDto> getBookingById(@PathVariable Long id)
    {
        BookingCreateDto data = bookingService.find3(id)
                    .map(t -> bookingMapper.toCreateDto(t))
                    .orElseThrow(BookingNotFoundException::new);

        return ResponseEntity.status(HttpStatus.FOUND).body(data);
    }
    @PostMapping("/bookings/flight/{flightId}/user/{userId}")
    public ResponseEntity<BookingCreateDto> createBooking(@PathVariable Long flightId, @PathVariable Long userId, @RequestBody BookingDto bookingDto)
    {
        Booking booking1 = bookingMapper.toEntity(bookingDto);
        Booking booking2 = bookingService.create(flightId, userId,booking1);
        BookingCreateDto bookingCreatedDto = bookingMapper.toCreateDto(booking2);
        if(booking2 == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(bookingCreatedDto);
        else
            return ResponseEntity.status(HttpStatus.CREATED).body(bookingCreatedDto);

        //URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(booking2.getId()).toUri();
        //return ResponseEntity.created(location).body(bookingCreatedDto);
    }
    @GetMapping("/bookings/flight/{flightId}")
    public ResponseEntity<List<BookingCreateDto>> getBookingsByFlightId(@PathVariable Long flightId)
    {
        List<Booking> bookings = bookingService.find2(flightId);
        List<BookingCreateDto> bookingsDto = bookings.stream().map(bookingMapper::toCreateDto).collect(Collectors.toList());
        if(bookingsDto.isEmpty())
            return ResponseEntity.noContent().build();
        else
        {
            return ResponseEntity.ok(bookingsDto);
        }
    }
    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<BookingCreateDto> deleteBooking(@PathVariable Long id)
    {
        Optional<Booking> bookingToDelete = bookingService.find3(id);
        BookingCreateDto data = bookingToDelete
        .map(t -> bookingMapper.toCreateDto(t))
        .orElseThrow(BookingNotFoundException::new);
        if(bookingToDelete.isPresent())
        {
            bookingService.delete(id);
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        else
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


}