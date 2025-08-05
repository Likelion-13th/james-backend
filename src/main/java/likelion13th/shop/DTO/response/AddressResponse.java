package likelion13th.shop.DTO.response;

import likelion13th.shop.domain.Address;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AddressResponse {
    private String zipcode;
    private String address;
    private String addressDetail;

    public static AddressResponse from(Address address){
        return new AddressResponse (
                address.getZipcode(),
                address.getAddress(),
                address.getAddressDetail()
        );
    }
}
