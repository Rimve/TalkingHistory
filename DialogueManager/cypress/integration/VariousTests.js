describe("homepage test", () => {
    it("navbar renders properly", () => {
        cy.visit("/")
        cy.get("#navbar").should("exist")
    })
    it("body renders properly", () => {
        cy.visit("/")
        cy.get("#body").should("exist")
    })
    it("footer renders properly", () => {
        cy.visit("/")
        cy.get("#footer").should("exist")
    })
    it("check if homepage renders properly", () => {
        cy.visit("/")
        cy.get("#home-page-image").should("exist")
    })
    it("check if login button is present", () => {
        cy.visit("/")
        cy.get(':nth-child(1) > a > .button > b').should("exist")
    })
    it("check if register button is present", () => {
        cy.visit("/")
        cy.get(':nth-child(2) > a > .button > b').should("exist")
    })
    it("check if navbar title is present", () => {
        cy.visit("/")
        cy.get('.title > b').should("exist")
    })
})
describe("management page tests", () => {
    it("check if page is not accessible while not logged in", () => {
        cy.visit("/management")
        cy.get("#character-grid").should("not.exist")
    })
    it("try to login", () => {
        cy.get(':nth-child(1) > a > .button > b').click()
        cy.get('[name="email"]').type("test@test.com")
        cy.get('[name="password"]').type("lopas123")
        cy.get('.button-login > b').click()
        cy.get(':nth-child(1) > a > .button > b').should("not.exist")
    })
    it("check if the graph renders", () => {
        cy.visit("/management")
        cy.get(':nth-child(1) > .text-decoration-none').click()
        cy.get('[data-id="layer0-selectbox"]').should("exist")
    })
    it("check if character grid is available", () => {
        cy.visit("/management")
        cy.get("#character-grid").should("exist")
    })
    it("try to create a new character", () => {
        cy.visit("/management")
        cy.get('.character-item').click()
        cy.get('.name-width > .form-control').type("AutoCharacter")
        cy.get('.align-items-center > .form-control').type("Character created by automated test")
        cy.get('div > :nth-child(2) > b').click()
        cy.get(':nth-child(1) > .character-banner > .character-text').contains("AutoCharacter")
    })
    it("try to delete character", () => {
        cy.visit("/management")
        cy.get('#button-AutoCharacter').click()
        cy.get('.delete-btn > b').click()
        cy.get('.MuiDialogActions-root > :nth-child(2) > b').click()
        cy.get('.MuiAlert-message').should("exist")
        cy.get(':nth-child(1) > .character-banner > .character-text').contains("AutoCharacter").should("not.exist")
    })
    it("try to logout", () => {
        cy.get(':nth-child(4) > .icon > span > b').click()
        cy.get(':nth-child(1) > a > .button > b').should("exist")
    })
})
describe("register test", () => {
    it("check if registration user icon is present", () => {
        cy.visit("/")
        cy.get(':nth-child(2) > a > .button > b').click()
        cy.get('.icon-user').should("exist").click()
    })
    it("check register form is present", () => {
        cy.get('.form-register').should("exist")
    })
    it("check if email field is present", () => {
        cy.get('#email').should("exist")
    })
    it("check if password field is present", () => {
        cy.get('#password').should("exist")
    })
    it("check if repeat password field is present", () => {
        cy.get('#passwordRpt').should("exist")
    })
    it("check if registration fails with existing user", () => {
        cy.visit("/")
        cy.get(':nth-child(2) > a > .button > b').click()
        cy.get('#email').type("auto@test.com")
        cy.get('#password').type("test123456")
        cy.get('#passwordRpt').type("test123456")
        cy.get('.button-register > b').click()
        cy.get('.MuiPaper-root').should("exist").click()
    })
})
describe("categories test", () => {
    it("try to login", () => {
        cy.get(':nth-child(1) > a > .button > b').click()
        cy.get('[name="email"]').type("test@test.com")
        cy.get('[name="password"]').type("lopas123")
        cy.get('.button-login > b').click()
        cy.get(':nth-child(1) > a > .button > b').should("not.exist")
    })
    it("check if categories are present", () => {
        cy.visit("/")
        cy.get(':nth-child(2) > .icon > span > b').click()
        cy.get('.list-group').should("exist")
    })
    it("try to create category", () => {
        cy.get('button.character-item').click()
        cy.get('.form-control').type("Auto test category")
        cy.get(':nth-child(2) > :nth-child(2) > b').click()
        cy.contains('Auto test category').should("exist")
    })
    it("check if modal visible on delete", () => {
        cy.contains('Delete').click()
        cy.get('.MuiDialog-container').should("exist")
    })
    it("logout after tests", () => {
        cy.visit("/")
        cy.get(':nth-child(4) > .icon > span > b').click()
        cy.get(':nth-child(1) > a > .button > b').should("exist")
    })
})